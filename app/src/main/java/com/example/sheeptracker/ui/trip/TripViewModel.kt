package com.example.sheeptracker.ui.trip

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.Trip
import com.example.sheeptracker.database.entities.TripMapPoint
import kotlinx.coroutines.*

class TripViewModel(
    private val tripId: Long,
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {


    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val locationManager = application.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val gpsListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            location?.let {
                latestLocation.value = location
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String?) {}
        override fun onProviderDisabled(provider: String?) {}
    }

    /** Fields **/

    val trip: LiveData<Trip?> = appDao.getTripLD(tripId)
    val mapArea: LiveData<MapArea?> = appDao.getMapAreaForTripLD(tripId)
    val tripMapPoints: LiveData<List<TripMapPoint>> = appDao.getTripMapPointsForTripLD(tripId)
    val observations: LiveData<List<Observation>> = appDao.getObservationsForTripLDAsc(tripId)
    val isTripFinished: LiveData<Boolean?> = appDao.isTripFinishedLD(tripId)

    val isTrackingGPS = MutableLiveData<Boolean>(false)
    val isFollowingGPS = MutableLiveData<Boolean>(true)

    val latestLocation = MutableLiveData<Location>()

    var motionLayoutProgress = 0.0f

    val herdObservationCount = Transformations.map(observations){
        observations.value?.let {
            return@map it.filter { obs -> obs.observationType == Observation.ObservationType.COUNT }.count()
        }
        0
    }

    val deadObservationCount = Transformations.map(observations){
        observations.value?.let {
            return@map it.filter { obs -> obs.observationType == Observation.ObservationType.DEAD }.count()
        }
        0
    }

    val injuredObservationCount = Transformations.map(observations){
        observations.value?.let {
            return@map it.filter { obs -> obs.observationType == Observation.ObservationType.INJURED }.count()
        }
        0
    }

    init {
        startLocationUpdates()
    }

    /** Methods **/

    fun deleteTrip() {
        uiScope.launch {
            delete(tripId)
        }
    }

    fun toggleFollowGPS() {
        if (isFollowingGPS.value!!) {
            stopLocationUpdates()
        } else {
            triggerLocationLiveDataUpdate()
            startLocationUpdates()
        }
        isFollowingGPS.value = !isFollowingGPS.value!!
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        //TODO: Use preferences values?
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5.0f, gpsListener)
    }

    fun stopLocationUpdates() {
        locationManager.removeUpdates(gpsListener)
    }

    fun triggerLocationLiveDataUpdate() {
        if (latestLocation.value != null) {
            latestLocation.value = latestLocation.value
        }
    }

    fun getTripMapPoint(id: Long): TripMapPoint? {
        var point: TripMapPoint? = null
        runBlocking {
            point = getTripMapPointDb(id)
        }
        return point
    }

    fun onFinishTrip() {
        uiScope.launch {
            finishTrip()
        }
    }

    /** Helpers **/

    private suspend fun delete(tripId: Long) {
        return withContext(Dispatchers.IO) {
            appDao.deleteTrip(tripId)
        }
    }

    private suspend fun getTripMapPointDb(id: Long): TripMapPoint? {
        return withContext(Dispatchers.IO) {
            return@withContext appDao.getTripMapPoint(id)
        }
    }

    private suspend fun finishTrip() {
        withContext(Dispatchers.IO) {
            appDao.finishTrip(tripId)
        }
    }

}