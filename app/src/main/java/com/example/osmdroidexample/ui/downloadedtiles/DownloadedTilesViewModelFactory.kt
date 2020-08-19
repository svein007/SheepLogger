package com.example.osmdroidexample.ui.downloadedtiles

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.osmdroidexample.database.AppDao

class DownloadedTilesViewModelFactory(
    private val application: Application,
    private val appDao: AppDao) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DownloadedTilesViewModel::class.java)) {
            return DownloadedTilesViewModel(application, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}