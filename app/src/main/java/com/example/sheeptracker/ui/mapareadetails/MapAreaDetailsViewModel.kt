package com.example.sheeptracker.ui.mapareadetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.sheeptracker.database.AppDao

class MapAreaDetailsViewModel(
    private val mapAreaId: Long,
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    val mapArea = appDao.getMapAreaLD(mapAreaId)
    val observationCount = appDao.getObservationCountForMapAreaLD(mapAreaId)
    val tripCount = appDao.getTripCountLD(mapAreaId)
    val deadAnimalCount = appDao.getDeadAnimalCountForMapAreaLD(mapAreaId)
    val injuredAnimalCount = appDao.getInjuredAnimalCountForMapAreaLD(mapAreaId)

}