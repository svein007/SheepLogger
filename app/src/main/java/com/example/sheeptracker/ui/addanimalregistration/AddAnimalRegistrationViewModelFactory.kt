package com.example.sheeptracker.ui.addanimalregistration

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.TripMapPoint

class AddAnimalRegistrationViewModelFactory(
    private val tripId: Long,
    private val obsLat: Double,
    private val obsLon: Double,
    private val currentPosition: TripMapPoint,
    private val observationType: Observation.ObservationType,
    private val application: Application,
    private val appDao: AppDao
)
    : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddAnimalRegistrationViewModel::class.java)) {
            return AddAnimalRegistrationViewModel(tripId, obsLat, obsLon, currentPosition, observationType, application, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}