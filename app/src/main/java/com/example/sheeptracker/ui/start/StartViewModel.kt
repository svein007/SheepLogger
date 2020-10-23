package com.example.sheeptracker.ui.start

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Trip

class StartViewModel(
    private val appDao: AppDao,
    application: Application) : AndroidViewModel(application) {

    val activeTrip: LiveData<Trip?> = appDao.getActiveTrip()

    val observationCount = appDao.getObservationCountForActiveTrip()

    val tripIsActive = Transformations.map(activeTrip) {
        it != null && !it.tripFinished
    }


}