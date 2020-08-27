package com.example.osmdroidexample.ui.maparea

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.MapArea
import com.example.osmdroidexample.map.MapAreaManager
import kotlinx.coroutines.*

class MapAreaViewModel(
    mapAreaId: Long,
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel

    val mapArea: LiveData<MapArea?>

    init {
        mapArea = appDao.getMapAreaLD(mapAreaId)
    }

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** ViewModel Methods **/

    fun deleteMapArea() {
        uiScope.launch {
            deleteMapAreaDb()
        }
    }

    /** Helpers **/

    private suspend fun deleteMapAreaDb() {
        withContext(Dispatchers.IO) {
            mapArea.value?.let {
                appDao.deleteMapArea(it.mapAreaId)

                val mapAreaString = it.getSqliteFilename()
                val app = getApplication<Application>()

                MapAreaManager.deleteMapArea(app.applicationContext, mapAreaString)
            }
        }
    }

}