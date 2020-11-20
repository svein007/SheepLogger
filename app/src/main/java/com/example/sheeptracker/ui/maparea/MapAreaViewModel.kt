package com.example.sheeptracker.ui.maparea

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.map.MapAreaManager
import kotlinx.coroutines.*

class MapAreaViewModel(
    mapAreaId: Long,
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    val mapArea: LiveData<MapArea?> = appDao.getMapAreaLD(mapAreaId)

}