package com.example.sheeptracker.ui.mapareadetails

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao

class MapAreaDetailsViewModelFactory(
    private val mapAreaId: Long,
    private val application: Application,
    private val appDao: AppDao
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapAreaDetailsViewModel::class.java)) {
            return MapAreaDetailsViewModel(mapAreaId, application, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}