package com.example.osmdroidexample.ui.addtrip

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.osmdroidexample.database.AppDao

class AddTripViewModelFactory(
    private val application: Application,
    private val appDao: AppDao
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTripViewModel::class.java)) {
            return AddTripViewModel(application, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}