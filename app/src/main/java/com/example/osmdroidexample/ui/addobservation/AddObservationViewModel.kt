package com.example.osmdroidexample.ui.addobservation

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.Observation
import com.example.osmdroidexample.database.entities.Trip
import kotlinx.coroutines.*

class AddObservationViewModel(
    tripId: Long,
    application: Application,
    private val appDao: AppDao) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val trip: LiveData<Trip?> = appDao.getTripLD(tripId)

    val observationNote = MutableLiveData<String>()
    val observationTripId = MutableLiveData<String>()

    /** Methods **/

    fun addObservation(onSuccess: () -> Unit, onFail: () -> Unit) {
        uiScope.launch {
            observationNote.value?.let {
                if (it.isBlank())
                    return@let

                val tripIdLong = observationTripId.value?.toLongOrNull()

                if (tripIdLong != null) {
                    try {
                        val observation = Observation(
                            observationNote = it,
                            observationOwnerTripId = tripIdLong
                        )

                        insert(observation)

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

    private suspend fun insert(observation: Observation): Long {
        return withContext(Dispatchers.IO) {
            appDao.insert(observation)
        }
    }
}