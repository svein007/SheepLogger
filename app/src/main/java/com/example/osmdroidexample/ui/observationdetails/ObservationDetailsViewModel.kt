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

    val observationNote = MutableLiveData<String>()
    val observationSheepCount = MutableLiveData<Int>(0)
    val observationLambCount = MutableLiveData<Int>(0)

    init {
        uiScope.launch {
            observation.value = getObservation(observationId)

            observation.value?.let {
                observationNote.value = it.observationNote
                observationSheepCount.value = it.observationSheepCount
                observationLambCount.value = it.observationLambCount
            }
        }
    }

    /** ViewModel methods **/

    fun incSheepCount() {
        observationSheepCount.value?.let {
            observationSheepCount.value = it + 1
        }
    }

    fun decSheepCount() {
        observationSheepCount.value?.let {
            if (it > 0) {
                observationSheepCount.value = it - 1
            }
        }
    }

    fun incLambCount() {
        observationLambCount.value?.let {
            observationLambCount.value = it + 1
        }
    }

    fun decLambCount() {
        observationLambCount.value?.let {
            if (it > 0) {
                observationLambCount.value = it - 1
            }
        }
    }

    fun onUpdateObservation() {
        uiScope.launch {
            if (observation.value != null) {
                observation.value!!.observationNote = observationNote.value!!
                observation.value!!.observationSheepCount = observationSheepCount.value!!
                observation.value!!.observationLambCount = observationLambCount.value!!

                updateObservation(observation.value!!)
            }
        }
    }

    /** Helpers **/

    private suspend fun updateObservation(observation: Observation) {
        withContext(Dispatchers.IO) {
            appDao.update(observation)
        }
    }

    private suspend fun getObservation(id: Long): Observation? {
        return withContext(Dispatchers.IO) {
            appDao.getObservation(id)
        }
    }

}