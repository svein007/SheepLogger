package com.example.sheeptracker.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.MapArea
import kotlinx.coroutines.*

class MainViewModel(
    application: Application,
    private val appDao: AppDao
    ) : AndroidViewModel(application) {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

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