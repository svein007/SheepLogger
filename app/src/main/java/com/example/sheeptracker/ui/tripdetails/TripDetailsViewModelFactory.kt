package com.example.sheeptracker.ui.tripdetails

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao

class TripDetailsViewModelFactory(
    private val tripId: Long,
    private val application: Application,
    private val appDao: AppDao
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripDetailsViewModel::class.java)) {
            return TripDetailsViewModel(tripId, application, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}