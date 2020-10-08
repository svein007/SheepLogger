package com.example.sheeptracker.ui.animalcountersdetails

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Counter
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.ui.swiper.SwiperViewModel
import kotlinx.coroutines.*

class HerdObservationDetailsViewModel(
    private val observationId: Long,
    app: Application,
    private val appDao: AppDao
) : SwiperViewModel(app) {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** VM fields **/

    override val observation = appDao.getObservationLD(observationId)

    override val counters = appDao.getCountersLD(observationId)

    override val countType = MutableLiveData<Counter.CountType>(Counter.CountType.SHEEP)

    val expectedLambCount = Transformations.map(counters) {
        it.sumBy { counter -> counter.sheepChildCount() }
    }

    val lambCount = Transformations.map(counters) {
        it.firstOrNull { c -> c.counterType == Counter.CountType.LAMB }?.let { counter ->
            return@map counter.counterValue
        }
        0
    }

    /** VM Methods **/

    fun onUpdateObservation() {
        uiScope.launch {
            if (observation.value != null) {
                updateObservation(observation.value!!)
                updateCounters()
            }
        }
    }

    fun onDeleteObservation() {
        uiScope.launch {
            delete()
        }
    }

    fun onUpdateCounter(counter: Counter) {
        uiScope.launch {
            updateCounter(counter)
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

    private suspend fun updateCounter(counter: Counter) {
        withContext(Dispatchers.IO) {
            appDao.update(counter)
        }
    }

    private suspend fun delete() {
        withContext(Dispatchers.IO) {
            counters.value?.let {
                for (counter in it) {
                    appDao.deleteCounter(counter.counterId)
                }
            }

            observation.value?.let {
                appDao.deleteObservation(it.observationId)
            }
        }
    }

}