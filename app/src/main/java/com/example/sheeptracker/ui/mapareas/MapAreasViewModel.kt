package com.example.sheeptracker.ui.mapareas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.MapArea

class MapAreasViewModel(
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    val mapAreas: LiveData<List<MapArea>> = appDao.getMapAreasLD()

    val showEmptyListTextView = Transformations.map(mapAreas) {
        it.isNullOrEmpty()
    }
}