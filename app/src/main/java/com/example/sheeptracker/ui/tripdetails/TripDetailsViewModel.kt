package com.example.sheeptracker.ui.tripdetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.MapArea

class TripDetailsViewModel(
    private val tripId: Long,
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    val trip = appDao.getTripLD(tripId)
    val mapArea: LiveData<MapArea?> = appDao.getMapAreaForTripLD(tripId)
    val observationCount: LiveData<Int> = appDao.getObservationCountForTripLD(tripId)

}