package com.example.sheeptracker.ui.start

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao

class StartViewModelFactory(
    private val appDao: AppDao,
    private val application: Application
) : ViewModelProvider.Factory  {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StartViewModel::class.java)) {
            return StartViewModel(appDao, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}