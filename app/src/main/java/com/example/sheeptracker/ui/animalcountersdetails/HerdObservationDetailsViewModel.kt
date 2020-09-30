package com.example.sheeptracker.ui.animalcountersdetails

import android.app.Application
import androidx.lifecycle.MutableLiveData
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