package com.example.osmdroidexample.ui.trip

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.MapArea
import kotlinx.coroutines.*

class TripViewModel(
    private val mapAreaId: Long,
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    val mapArea: LiveData<MapArea?>

    init {
        mapArea = appDao.getMapAreaLD(mapAreaId)
        Log.d("TripViewModel", "MapArea: " + (mapArea.value?.mapAreaName ?: "???"))
    }

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


}