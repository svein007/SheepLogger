package com.example.osmdroidexample.ui.observations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.Observation
import com.example.osmdroidexample.database.entities.Trip

class ObservationsViewModel(
    tripId: Long,
    appDao: AppDao,
    application: Application
) : AndroidViewModel(application) {

    val trip: LiveData<Trip?> = appDao.getTripLD(tripId)

    val observations: LiveData<List<Observation>> = appDao.getObservationsForTripLD(tripId)

}