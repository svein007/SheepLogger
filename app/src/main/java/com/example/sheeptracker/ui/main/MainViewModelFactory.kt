package com.example.sheeptracker.ui.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao

class MainViewModelFactory(
    private val application: Application,
    private val appDao: AppDao) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}