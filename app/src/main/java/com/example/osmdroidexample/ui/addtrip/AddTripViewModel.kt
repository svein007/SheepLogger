package com.example.osmdroidexample.ui.addtrip

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.osmdroidexample.database.AppDao
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

    val tripName = MutableLiveData<String>()

    /** ViewModel methods **/

    fun addTrip() {
        uiScope.launch {
            tripName.value?.let {
                if (it.isBlank())
                    return@let

                val trip = Trip(
                    tripName = it,
                    tripDate = dateToFormattedString(getToday()),
                    tripOwnerMapAreaId = getMapAreaId()
                )

                insert(trip)
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