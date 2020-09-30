package com.example.sheeptracker.ui.animalcountersdetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Observation
import kotlinx.coroutines.*

class HerdObservationDetailsViewModel(
    private val observationId: Long,
    app: Application,
    private val appDao: AppDao
) : AndroidViewModel(app) {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** VM fields **/

    val observation = appDao.getObservationLD(observationId)

    val counters = appDao.getCountersLD(observationId)

    /** VM Methods **/

    fun onUpdateObservation() {
        uiScope.launch {
            if (observation.value != null) {
                updateObservation(observation.value!!)
                updateCounters()
            }
        }
    }

    /** Helpers **/

    private suspend fun updateObservation(observation: Observation) {
        withContext(Dispatchers.IO) {
            appDao.update(observation)
        }
    }

    private suspend fun updateCounters() {
        withContext(Dispatchers.IO) {
            for (counter in counters.value!!) {
                appDao.update(counter)
            }
        }
    }


}