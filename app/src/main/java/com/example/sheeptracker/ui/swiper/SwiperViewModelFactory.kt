package com.example.sheeptracker.ui.swiper

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao

class SwiperViewModelFactory(
    private val observationId: Long,
    private val appDao: AppDao,
    private val application: Application
) : ViewModelProvider.Factory  {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SwiperViewModel::class.java)) {
            return SwiperViewModel(observationId, application, appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}