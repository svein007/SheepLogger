package com.example.sheeptracker.ui.observations

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao

class ObservationsViewModelFactory(
    private val tripId: Long,
    private val appDao: AppDao,
    private val application: Application
) : ViewModelProvider.Factory  {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ObservationsViewModel::class.java)) {
            return ObservationsViewModel(tripId, appDao, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}