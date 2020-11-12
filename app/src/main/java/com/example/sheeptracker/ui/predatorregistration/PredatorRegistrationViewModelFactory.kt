package com.example.sheeptracker.ui.predatorregistration

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.database.AppDao

class PredatorRegistrationViewModelFactory(
    private val obsId: Long,
    private val tripId: Long,
    private val obsLat: Double,
    private val obsLon: Double,
    private val obsType: Int,
    private val application: Application,
    private val appDao: AppDao
): ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PredatorRegistrationViewModel::class.java)) {
            return PredatorRegistrationViewModel(
                obsId,
                tripId,
                obsLat,
                obsLon,
                obsType,
                application,
                appDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}