package com.example.sheeptracker.ui.addobservation

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.*
import com.example.sheeptracker.utils.*
import kotlinx.coroutines.*
import java.util.*

class AddObservationViewModel(
    private val tripId: Long,
    private val currentPosition: TripMapPoint,
    application: Application,
    private val appDao: AppDao) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val observationDate = Date()

    val trip: LiveData<Trip?> = appDao.getTripLD(tripId)

    private val _observation = MutableLiveData<Observation>()
    val observation: LiveData<Observation>
        get() = _observation

    private val _counters = MutableLiveData<List<Counter>>()
    val counters: LiveData<List<Counter>>
        get() =_counters

    val countType = MutableLiveData<Counter.CountType>(Counter.CountType.SHEEP)

    val expectedSheepCount = Transformations.map(counters) {
        it.sumBy { counter -> counter.sheepChildCount() }
    }

    init {
        val newObservation = Observation(
            observationLat = 0.0,
            observationLon = 0.0,
            observationNote = "",
            observationDate = observationDate,
            observationOwnerTripId = tripId,
            observationOwnerTripMapPointId = -1,
            observationType = Observation.ObservationType.COUNT
        )

        _observation.value = newObservation

        val newCounters = mutableListOf<Counter>()
        for (countType in Counter.CountType.values()) {
            val counter = Counter(
                counterOwnerObservationId = -1,
                counterType = countType
            )
            newCounters.add(counter)
        }

        _counters.value = newCounters

    }

    /** Methods **/

    fun addObservation(lat: Double, lon: Double, onSuccess: () -> Unit, onFail: () -> Unit) {
        uiScope.launch {
            try {
                val observationPoint = getObservedFromPoint(appDao, tripId, currentPosition)

                observation.value?.apply {
                    observationLat = lat
                    observationLon = lon
                    observationOwnerTripMapPointId = observationPoint.tripMapPointId
                }

                val obsId = observation.value?.let { insert(it) }

                if (obsId != null && counters.value != null) {
                    for (counter in counters.value!!) {
                        counter.counterOwnerObservationId = obsId
                        insert(counter)
                    }
                }

                onSuccess()
            } catch (e: SQLiteConstraintException) {
                onFail()
            }
        }
    }

    /* Forces the counters LiveData to propagate to Transformations
       when fields of Counters in the list have updated values.
     */
    fun forceCountersLiveDateUpdateHack() {
        _counters.value = _counters.value
    }

    /** Helpers **/

    private suspend fun insert(observation: Observation): Long {
        return withContext(Dispatchers.IO) {
            appDao.insert(observation)
        }
    }

    private suspend fun insert(counter: Counter): Long {
        return withContext(Dispatchers.IO) {
            appDao.insert(counter)
        }
    }

}