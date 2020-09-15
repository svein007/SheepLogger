package com.example.sheeptracker.ui.trip

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.Trip
import com.example.sheeptracker.database.entities.TripMapPoint
import kotlinx.coroutines.*
import java.util.*

class TripViewModel(
    private val tripId: Long,
    private val mapAreaId: Long,
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {


    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** Fields **/

    val trip: LiveData<Trip?> = appDao.getTripLD(tripId)
    val mapArea: LiveData<MapArea?> = appDao.getMapAreaLD(mapAreaId)
    val tripMapPoints: LiveData<List<TripMapPoint>> = appDao.getTripMapPointsForTripLD(tripId)
    val observations: LiveData<List<Observation>> = appDao.getObservationsForTripLD(tripId)

    val isTrackingGPS = MutableLiveData<Boolean>(false)

    /** Methods **/

    fun addTripMapPoint(lat: Double, lon: Double, dateTime: Date) {
        uiScope.launch {
            val point = TripMapPoint(
                tripMapPointLat = lat,
                tripMapPointLon = lon,
                tripMapPointDate = dateTime,
                tripMapPointOwnerTripId = tripId
            )

        }
    }

    fun deleteTrip() {
        uiScope.launch {
            delete(tripId)
        }
    }

    /** Helpers **/

    private suspend fun delete(tripId: Long) {
        return withContext(Dispatchers.IO) {
            appDao.deletTrip(tripId)
        }
    }


}