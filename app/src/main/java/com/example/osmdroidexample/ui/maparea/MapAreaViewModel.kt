package com.example.osmdroidexample.ui.maparea

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.MapArea

class MapAreaViewModel(
    mapAreaId: Long,
    application: Application,
    appDao: AppDao
) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel

    val mapArea: LiveData<MapArea?>

    init {
        mapArea = appDao.getMapAreaLD(mapAreaId)
    }

}