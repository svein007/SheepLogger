package com.example.osmdroidexample.ui.addobservation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.Trip

class AddObservationViewModel(
    tripId: Long,
    application: Application,
    appDao: AppDao) : AndroidViewModel(application) {

    val trip: LiveData<Trip?> = appDao.getTripLD(tripId)

}