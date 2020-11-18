package com.example.sheeptracker.ui.herdobservationdetails

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Counter
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.TripMapPoint
import com.example.sheeptracker.map.MapAreaManager
import com.example.sheeptracker.utils.getObservedFromPoint
import kotlinx.coroutines.*
import org.osmdroid.util.GeoPoint
import java.util.*

class HerdObservationDetailsViewModel(
    private val observationId: Long,
    private val tripId: Long,
    private val obsLat: Double,
    private val obsLon: Double,
    app: Application,
    private val appDao: AppDao
) : AndroidViewModel(app) {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** VM fields **/

    private val obsIdLD = MutableLiveData<Long>(observationId)

    var obsId = observationId

    val observation = Transformations.switchMap(obsIdLD) {
        appDao.getObservationLD(it)
    }

    val counters = Transformations.switchMap(obsIdLD) {
        appDao.getCountersLD(it)
    }

    val expectedLambCount = Transformations.map(counters) {
        it.sumBy { counter -> counter.sheepChildCount() }
    }

    val lambCount = Transformations.map(counters) {
        it.firstOrNull { c -> c.counterType == Counter.CountType.LAMB }?.let { counter ->
            return@map counter.counterValue
        }
        0
    }

    val trip = Transformations.switchMap(obsIdLD) {
        appDao.getTripForObservation(observationId)
    }

    init {
        uiScope.launch {
            val newObs = observationId < 0
            if (newObs) {
                if (obsLat.isNaN() || obsLon.isNaN()) {
                    Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.unable_gps), Toast.LENGTH_LONG).show()
                    return@launch
                }

                MapAreaManager.getLastKnownLocation(
                    getApplication<Application>().applicationContext,
                    null,
                    1
                )?.let {
                    val currentPosition = TripMapPoint(
                        tripMapPointLon =  it.longitude,
                        tripMapPointLat = it.latitude ,
                        tripMapPointDate = Date(),
                        tripMapPointOwnerTripId = tripId
                    )

                    val observationPoint = getObservedFromPoint(appDao, tripId, currentPosition)

                    val observationType = Observation.ObservationType.COUNT

                    val newObservation = Observation(
                        observationLat = obsLat,
                        observationLon = obsLon,
                        observationNote = "",
                        observationDate = Date(),
                        observationOwnerTripId = tripId,
                        observationOwnerTripMapPointId = observationPoint.tripMapPointId,
                        observationType = observationType
                    )

                    val insertedObsId = insert(newObservation)
                    obsIdLD.value = insertedObsId
                    obsId = insertedObsId

                    for (countType in Counter.CountType.values()) {
                        val counter = Counter(
                            counterOwnerObservationId = insertedObsId,
                            counterType = countType
                        )
                        insert(counter)
                    }
                }
            }
        }
    }

    /** VM Methods **/

    fun onUpdateObservation() {
        uiScope.launch {
            if (observation.value != null) {
                updateObservation(observation.value!!)
                updateCounters()
            }
        }
    }

    fun onDeleteObservation() {
        uiScope.launch {
            delete()
        }
    }

    fun onUpdateCounter(counter: Counter) {
        uiScope.launch {
            updateCounter(counter)
        }
    }

    fun onAddSecondaryTripMapPoint(currentPosition: GeoPoint) {
        uiScope.launch {
            addSecondaryTripMapPoint(currentPosition)
        }
    }

    fun onDeleteSecondaryTripMapPoint() {
        uiScope.launch {
            deleteSecondaryTripMapPoint()
        }
    }

    /** Helpers **/

    private suspend fun updateObservation(observation: Observation) {
        withContext(Dispatchers.IO) {
            appDao.update(observation)
        }
    }

    private suspend fun updateCounters() {
        withContext(Dispatchers.IO) {
            for (counter in counters.value!!) {
                appDao.update(counter)
            }
        }
    }

    private suspend fun updateCounter(counter: Counter) {
        withContext(Dispatchers.IO) {
            appDao.update(counter)
        }
    }

    private suspend fun delete() {
        withContext(Dispatchers.IO) {
            counters.value?.let {
                for (counter in it) {
                    appDao.deleteCounter(counter.counterId)
                }
            }

            observation.value?.let {
                appDao.deleteObservation(it.observationId)
            }
        }
    }

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

    private suspend fun addSecondaryTripMapPoint(currentPosition: GeoPoint) {
        withContext(Dispatchers.IO) {
            val tripId = observation.value!!.observationOwnerTripId
            val tripMapPoint = getObservedFromPoint(
                appDao,
                tripId,
                TripMapPoint(
                    tripMapPointLat =  currentPosition.latitude,
                    tripMapPointLon =  currentPosition.longitude,
                    tripMapPointDate = Date(),
                    tripMapPointOwnerTripId = tripId
                )
            )

            if (observation.value!!.observationOwnerTripMapPointId != tripMapPoint.tripMapPointId) {
                observation.value!!.observationSecondaryTripMapPointId = tripMapPoint.tripMapPointId
                appDao.update(observation.value!!)
            }
        }
    }

    private suspend fun deleteSecondaryTripMapPoint() {
        withContext(Dispatchers.IO) {
            observation.value?.observationSecondaryTripMapPointId?.let {
                observation.value!!.observationSecondaryTripMapPointId = null
                appDao.update(observation.value!!)
            }
        }
    }

}