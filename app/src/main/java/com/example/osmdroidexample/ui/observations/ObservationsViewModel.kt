package com.example.osmdroidexample.ui.observations

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.Trip

class ObservationsViewModel(
    tripId: Long,
    appDao: AppDao
) : ViewModel() {

    val trip: LiveData<Trip?> = appDao.getTripLD(tripId)

}