package com.example.sheeptracker.ui.observations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.Trip

class ObservationsViewModel(
    tripId: Long,
    appDao: AppDao,
    application: Application
) : AndroidViewModel(application) {

    val trip: LiveData<Trip?> = appDao.getTripLD(tripId)

    val observations: LiveData<List<Observation>> = appDao.getObservationsForTripLD(tripId)

}