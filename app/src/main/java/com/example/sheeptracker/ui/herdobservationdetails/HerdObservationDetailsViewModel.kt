package com.example.sheeptracker.ui.herdobservationdetails

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Counter
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.TripMapPoint
import com.example.sheeptracker.ui.swiper.SwiperViewModel
import com.example.sheeptracker.utils.getObservedFromPoint
import kotlinx.coroutines.*
import org.osmdroid.util.GeoPoint
import java.util.*

class HerdObservationDetailsViewModel(
    private val observationId: Long,
    app: Application,
    private val appDao: AppDao
) : SwiperViewModel(app) {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** VM fields **/

    override val observation = appDao.getObservationLD(observationId)

    override val counters = appDao.getCountersLD(observationId)

    override val countType = MutableLiveData<Counter.CountType>(Counter.CountType.SHEEP)

    val expectedLambCount = Transformations.map(counters) {
        it.sumBy { counter -> counter.sheepChildCount() }
    }

    val lambCount = Transformations.map(counters) {
        it.firstOrNull { c -> c.counterType == Counter.CountType.LAMB }?.let { counter ->
            return@map counter.counterValue
        }
        0
    }

    val trip = appDao.getTripForObservation(observationId)

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