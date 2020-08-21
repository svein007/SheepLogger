package com.example.osmdroidexample.ui.mapareas

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.MapArea

class MapAreasViewModel(
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    val mapAreas: LiveData<List<MapArea>> = appDao.getMapAreasLD()

}