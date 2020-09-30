package com.example.sheeptracker.ui.animalcountersdetails

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao

class AnimalCountersDetailsViewModelFactory(
    private val observationId: Long,
    private val application: Application,
    private val appDao: AppDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HerdObservationDetailsViewModel::class.java)) {
            return HerdObservationDetailsViewModel(observationId, application, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}