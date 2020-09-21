package com.example.sheeptracker.ui.observationdetails

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.AnimalRegistration
import com.example.sheeptracker.database.entities.ImageResource
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.utils.getDrawableFromUri
import com.example.sheeptracker.utils.storeDrawableWithName
import kotlinx.coroutines.*

class ObservationDetailsViewModel(
    private val observationId: Long,
    app: Application,
    private val appDao: AppDao) : AndroidViewModel(app) {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val observation = MutableLiveData<Observation>()

    val counters = appDao.getCountersLD(observationId)

    val observationNote = MutableLiveData<String>()

    val deadAnimal = appDao.getDeadAnimal(observationId)

    val imageResources = appDao.getImageResourcesLD(observationId)

    val showDeadAnimal = Transformations.map(deadAnimal) {
        it != null
    }

    val observationTypeTitle = Transformations.map(observation) {
        when (observation.value?.observationType) {
            Observation.ObservationType.DEAD -> "DEAD ANIMAL"
            Observation.ObservationType.INJURED -> "INJURED ANIMAL"
            else -> "-"
        }
    }

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
                deadAnimal.value?.let {
                    updateDeadAnimal(it)
                }
            }
        }
    }

    fun addImageResource(imgUri: String) {
        uiScope.launch {
            addImgResToDB(imgUri)
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

    private suspend fun updateDeadAnimal(animalRegistration: AnimalRegistration) {
        withContext(Dispatchers.IO) {
            appDao.update(animalRegistration)
        }
    }

    private suspend fun getObservation(id: Long): Observation? {
        return withContext(Dispatchers.IO) {
            appDao.getObservation(id)
        }
    }

    /**
     * Creates and stores a copy of the given file, and creates a ImageResource entry in DB.
     */
    private suspend fun addImgResToDB(imgUri: String) {
        withContext(Dispatchers.IO) {
            val drawable = getDrawableFromUri(getApplication<Application>().applicationContext, Uri.parse(imgUri))
            val newImgRes = ImageResource(imageResourceObservationId = observationId)
            val newId = appDao.insert(newImgRes)
            val uriString = storeDrawableWithName(getApplication<Application>().applicationContext, drawable!!, "img_${observationId}_$newId")
            val imgRes = appDao.getImageResource(newId)

            imgRes.imageResourceUri = uriString

            appDao.update(imgRes)
        }
    }

}