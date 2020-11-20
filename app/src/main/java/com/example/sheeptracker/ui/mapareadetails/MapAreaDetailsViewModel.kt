package com.example.sheeptracker.ui.mapareadetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.map.MapAreaManager
import kotlinx.coroutines.*

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