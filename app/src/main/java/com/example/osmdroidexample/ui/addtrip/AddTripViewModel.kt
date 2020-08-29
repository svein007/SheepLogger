package com.example.osmdroidexample.ui.addtrip

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.MapArea
import com.example.osmdroidexample.database.entities.Trip
import com.example.osmdroidexample.utils.dateToFormattedString
import com.example.osmdroidexample.utils.getToday
import kotlinx.coroutines.*

class AddTripViewModel(
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** ViewModel fields **/

    val mapAreas: LiveData<List<MapArea>> = appDao.getMapAreasLD()

    val tripName = MutableLiveData<String>()
    val mapAreaId = MutableLiveData<String>()

    /** ViewModel methods **/

    fun addTrip(onSuccess: () -> Unit, onFail: () -> Unit) {
        uiScope.launch {
            tripName.value?.let {
                if (it.isBlank())
                    return@let

                val mapAreaIdLong = mapAreaId.value?.toLongOrNull()

                if (mapAreaIdLong != null) {
                    try {
                        val trip = Trip(
                            tripName = it,
                            tripDate = dateToFormattedString(getToday()),
                            tripOwnerMapAreaId = mapAreaIdLong
                        )

                        insert(trip)

                        onSuccess()
                    } catch (e: SQLiteConstraintException) {
                        onFail()
                    }
                } else {
                    onFail()
                }
            }
        }
    }

    /** Helpers **/

    private suspend fun insert(trip: Trip): Long {
        return withContext(Dispatchers.IO) {
            appDao.insert(trip)
        }
    }

    private suspend fun getMapAreaId(): Long {
        return withContext(Dispatchers.IO) {
            appDao.getMapAreas()[0].mapAreaId
        }
    }

}