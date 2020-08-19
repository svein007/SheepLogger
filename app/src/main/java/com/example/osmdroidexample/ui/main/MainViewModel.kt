package com.example.osmdroidexample.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.MapArea
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