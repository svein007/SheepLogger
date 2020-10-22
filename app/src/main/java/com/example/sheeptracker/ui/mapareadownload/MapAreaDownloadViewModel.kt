package com.example.sheeptracker.ui.mapareadownload

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.MapArea
import kotlinx.coroutines.*
import org.osmdroid.util.BoundingBox

class MapAreaDownloadViewModel(
    application: Application,
    private val appDao: AppDao
    ) : AndroidViewModel(application) {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** Public fields **/

    var tileCount = 0
    var boundingBox: BoundingBox? = null
    var minZoom = 0.0
    var maxZoom = 0.0

    val mapAreaName = MutableLiveData("")

    val downloadEnabled = Transformations.map(mapAreaName){
        it.isNotBlank()
    }

    /** ViewModel methods **/

    fun storeMapArea(mapArea: MapArea) {
        uiScope.launch {
            storeMapAreaDb(mapArea)
        }
    }

    /** Helpers **/

    private suspend fun storeMapAreaDb(mapArea: MapArea) {
        withContext(Dispatchers.IO) {
            appDao.insert(mapArea)
        }
    }


}