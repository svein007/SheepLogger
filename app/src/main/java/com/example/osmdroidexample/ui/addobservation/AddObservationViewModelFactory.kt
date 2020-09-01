package com.example.osmdroidexample.ui.addobservation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.TripMapPoint

class AddObservationViewModelFactory(
    private val tripId: Long,
    private val currentPosition: TripMapPoint,
    private val application: Application,
    private val appDao: AppDao
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddObservationViewModel::class.java)) {
            return AddObservationViewModel(tripId, currentPosition, application, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}