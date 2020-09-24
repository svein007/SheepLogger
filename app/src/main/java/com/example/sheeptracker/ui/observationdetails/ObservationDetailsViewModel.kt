package com.example.sheeptracker.ui.observationdetails

import android.app.Application
import android.graphics.drawable.Drawable
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
import java.text.SimpleDateFormat

class ObservationDetailsViewModel(
    private val observationId: Long,
    app: Application,
    private val appDao: AppDao) : AndroidViewModel(app) {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** VM fields **/

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

    val showEmptyImageListTextView = Transformations.map(imageResources) {
        it.isNullOrEmpty()
    }

    val observationTimeString = Transformations.map(observation) {
        SimpleDateFormat("HH:mm").format(it.observationDate)
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

    fun addImageResource(drawable: Drawable) {
        uiScope.launch {
            addImgResToDB(drawable)
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

    private suspend fun addImgResToDB(drawable: Drawable) {
        withContext(Dispatchers.IO) {
            val newImgRes = ImageResource(imageResourceObservationId = observationId)
            val newId = appDao.insert(newImgRes)
            val uriString = storeDrawableWithName(getApplication<Application>().applicationContext, drawable, "img_${observationId}_$newId")
            val imgRes = appDao.getImageResource(newId)

            imgRes.imageResourceUri = uriString

            appDao.update(imgRes)
        }
    }



}