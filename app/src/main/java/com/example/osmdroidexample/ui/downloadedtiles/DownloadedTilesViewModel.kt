package com.example.osmdroidexample.ui.downloadedtiles

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.MapArea

class DownloadedTilesViewModel(
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    val mapAreas: LiveData<List<MapArea>>

    init {
        mapAreas = appDao.getMapAreasLD()
        Log.d("#####", "MapAreas: " + (mapAreas.value?.joinToString(separator = ", ") { mapArea -> mapArea.mapAreaName }
            ?: ""))
    }

    /** Transformations **/

    val mapAreasString = Transformations.map(mapAreas) {
        return@map mapAreas.value?.joinToString(separator = ", ") { mapArea -> mapArea.mapAreaName } ?: "- Empty DB -"
    }

}