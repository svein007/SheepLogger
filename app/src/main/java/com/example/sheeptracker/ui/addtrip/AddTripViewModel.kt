package com.example.sheeptracker.ui.addtrip

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.database.entities.Trip
import kotlinx.coroutines.*
import java.util.*

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

    fun addTrip(onSuccess: (tripId: Long) -> Unit, onFail: () -> Unit) {
        uiScope.launch {
            tripName.value?.let {
                if (it.isBlank())
                    return@let

                val mapAreaIdLong = mapAreaId.value?.toLongOrNull()

                if (mapAreaIdLong != null) {
                    try {
                        val trip = Trip(
                            tripName = it,
                            tripDate = Date(),
                            tripOwnerMapAreaId = mapAreaIdLong
                        )

                        val tripId = insert(trip)

                        onSuccess(tripId)
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