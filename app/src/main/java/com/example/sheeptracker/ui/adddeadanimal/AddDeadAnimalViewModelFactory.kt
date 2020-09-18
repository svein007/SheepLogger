package com.example.sheeptracker.ui.adddeadanimal

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.TripMapPoint

class AddDeadAnimalViewModelFactory(
    private val tripId: Long,
    private val currentPosition: TripMapPoint,
    private val application: Application,
    private val appDao: AppDao
)
    : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddDeadAnimalViewModel::class.java)) {
            return AddDeadAnimalViewModel(tripId, currentPosition, application, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}