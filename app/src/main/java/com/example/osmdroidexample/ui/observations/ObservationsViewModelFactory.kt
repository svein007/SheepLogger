package com.example.osmdroidexample.ui.observations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.osmdroidexample.database.AppDao

class ObservationsViewModelFactory(
    private val mapAreaId: Long,
    private val appDao: AppDao
) : ViewModelProvider.Factory  {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ObservationsViewModel::class.java)) {
            return ObservationsViewModel(mapAreaId, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}