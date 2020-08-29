package com.example.osmdroidexample.ui.trip

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.MapArea
import com.example.osmdroidexample.database.entities.Trip
import com.example.osmdroidexample.database.entities.TripMapPoint
import kotlinx.coroutines.*

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

    /** Methods **/

    fun addTripMapPoint(lat: Double, lon: Double, dateTime: String) {
        uiScope.launch {
            val point = TripMapPoint(
                tripMapPointLat = lat,
                tripMapPointLon = lon,
                tripMapPointDate = dateTime,
                tripMapPointOwnerTripId = tripId
            )

            insert(point)
        }
    }

    /** Helpers **/

    private suspend fun insert(point: TripMapPoint): Long {
        return withContext(Dispatchers.IO) {
            appDao.insert(point)
        }
    }


}