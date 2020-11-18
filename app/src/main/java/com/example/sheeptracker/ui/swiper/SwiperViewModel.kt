package com.example.sheeptracker.ui.swiper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Counter
import com.example.sheeptracker.database.entities.Observation
import kotlinx.coroutines.*

class SwiperViewModel(
    private val observationId: Long,
    application: Application,
    private val appDao: AppDao) : AndroidViewModel(application) {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** VM fields **/

    val observation: LiveData<Observation?> = appDao.getObservationLD(observationId)
    val countType = MutableLiveData(Counter.CountType.SHEEP)
    val counters: LiveData<List<Counter>> = appDao.getCountersLD(observationId)

    /** VM Methods **/

    fun onUpdateObservation() {
        uiScope.launch {
            updateCounters()
        }
    }

    /** Helpers **/

    private suspend fun updateCounters() {
        withContext(Dispatchers.IO) {
            counters.value?.let {
                for (counter in counters.value!!) {
                    appDao.update(counter)
                }
            }
        }
    }

}