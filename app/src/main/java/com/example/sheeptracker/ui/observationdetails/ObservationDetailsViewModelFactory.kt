package com.example.sheeptracker.ui.observationdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao

class ObservationDetailsViewModelFactory(
    private val observationId: Long,
    private val appDao: AppDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ObservationDetailsViewModel::class.java)) {
            return ObservationDetailsViewModel(observationId, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}