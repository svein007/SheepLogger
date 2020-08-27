package com.example.osmdroidexample.ui.maparea

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.osmdroidexample.database.AppDao

class MapAreaViewModelFactory(
    private val mapAreaId: Long,
    private val application: Application,
    private val appDao: AppDao
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapAreaViewModel::class.java)) {
            return MapAreaViewModel(mapAreaId, application, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}