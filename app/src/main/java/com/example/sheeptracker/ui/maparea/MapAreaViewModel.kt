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