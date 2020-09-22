package com.example.sheeptracker.ui.imageresource

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao

class ImageResourceViewModelFactory(
    private val application: Application,
    private val appDao: AppDao,
    private val imageResourceId: Long,
    private val imageUri: String
) : ViewModelProvider.Factory  {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageResourceViewModel::class.java)) {
            return ImageResourceViewModel(application, appDao, imageResourceId, imageUri) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}