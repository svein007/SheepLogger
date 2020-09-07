package com.example.osmdroidexample.ui.observationdetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.Observation
import kotlinx.coroutines.*

class ObservationDetailsViewModel(
    observationId: Long,
    private val appDao: AppDao) : ViewModel() {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val observation = MutableLiveData<Observation>()

    val counters = appDao.getCountersLD(observationId)

    val observationNote = MutableLiveData<String>()

    init {
        uiScope.launch {
            observation.value = getObservation(observationId)

            observation.value?.let {
                observationNote.value = it.observationNote
            }
        }
    }

    /** ViewModel methods **/

    fun onUpdateObservation() {
        uiScope.launch {
            if (observation.value != null) {
                observation.value!!.observationNote = observationNote.value!!

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

    private suspend fun getObservation(id: Long): Observation? {
        return withContext(Dispatchers.IO) {
            appDao.getObservation(id)
        }
    }

}