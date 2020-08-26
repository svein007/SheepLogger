package com.example.osmdroidexample.ui.trips

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.Trip
import com.example.osmdroidexample.utils.dateToFormattedString
import com.example.osmdroidexample.utils.getToday
import kotlinx.coroutines.*

class TripsViewModel(
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** ViewModel fields **/

    val trips: LiveData<List<Trip>> = appDao.getTripsLD()

    /** ViewModel methods **/

    fun addTrip() {
        uiScope.launch {
            val trip = Trip(
                tripName = "My Trip",
                tripDate = dateToFormattedString(getToday()),
                tripOwnerMapAreaId = getMapAreaId()
            )

            insert(trip)
        }
    }

    /** Helpers **/

    private suspend fun insert(trip: Trip): Long {
        return withContext(Dispatchers.IO) {
            appDao.insert(trip)
        }
    }

    private suspend fun getMapAreaId(): Long {
        return withContext(Dispatchers.IO) {
            appDao.getMapAreas()[0].mapAreaId
        }
    }

}