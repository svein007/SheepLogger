package com.example.sheeptracker.ui.start

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Trip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StartViewModel(
    private val appDao: AppDao,
    application: Application) : AndroidViewModel(application) {

    val activeTrip: LiveData<Trip?> = appDao.getActiveTrip()

    var mapAreaCount: Int = 0

    val observationCount = appDao.getObservationCountForActiveTrip()

    val tripIsActive = Transformations.map(activeTrip) {
        it != null && !it.tripFinished
    }

    init {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                mapAreaCount = appDao.getMapAreaCount()
            }
        }
    }

}