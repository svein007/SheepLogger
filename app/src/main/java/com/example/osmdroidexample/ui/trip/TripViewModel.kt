package com.example.osmdroidexample.ui.trip

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.MapArea
import kotlinx.coroutines.*

class TripViewModel(private val mapAreaName: String,
                    application: Application,
                    private val appDao: AppDao
) : AndroidViewModel(application) {

    val mapArea: LiveData<MapArea?>

    init {
        Log.d("TripViewModel", "TripViewModel created!")
        mapArea = appDao.getMapAreaLD(mapAreaName)
        Log.d("TripViewModel", "MapArea: " + (mapArea.value?.mapAreaName ?: "???"))
    }

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** ViewModel methods **/


    override fun onCleared() {
        super.onCleared()
        Log.i("TripViewModel", "TripViewModel destroyed!")
    }

    /** Helpers **/

    private suspend fun getMapAreaDb(mapAreaName: String): MapArea? {
        return withContext(Dispatchers.IO) {
            return@withContext appDao.getMapArea(mapAreaName)
        }
    }

}