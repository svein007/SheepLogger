package com.example.sheeptracker.ui.animalregistrationdetails

import android.app.Application
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.AnimalRegistration
import com.example.sheeptracker.database.entities.ImageResource
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.utils.deleteFile
import com.example.sheeptracker.utils.getDrawableFromUri
import com.example.sheeptracker.utils.storeDrawableWithName
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class AnimalRegistrationDetailsViewModel(
    private val observationId: Long,
    app: Application,
    private val appDao: AppDao
) : AndroidViewModel(app) {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** VM fields **/

    val observation = appDao.getObservationLD(observationId)

    val animalRegistration = appDao.getDeadAnimal(observationId)

    val imageResources = appDao.getImageResourcesLD(observationId)

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
        it?.let {
            SimpleDateFormat("HH:mm").format(it.observationDate)
        }
        null
    }

    /** ViewModel methods **/

    fun onUpdateObservation() {
        uiScope.launch {
            updateObservation(observation.value!!)
            animalRegistration.value?.let {
                updateDeadAnimal(it)
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

    fun deleteObservation() {
        uiScope.launch {
            delete()
        }
    }

    /** Helpers **/

    private suspend fun updateObservation(observation: Observation) {
        withContext(Dispatchers.IO) {
            appDao.update(observation)
        }
    }

    private suspend fun updateDeadAnimal(animalRegistration: AnimalRegistration) {
        withContext(Dispatchers.IO) {
            appDao.update(animalRegistration)
        }
    }

    private suspend fun delete() {
        withContext(Dispatchers.IO) {
            imageResources.value?.let {
                for (imgRes in it) {
                    deleteFile(imgRes.getImgUri())
                }
            }

            animalRegistration.value?.let {
                appDao.deleteAnimalRegistration(it.id)
            }

            observation.value?.let {
                appDao.deleteObservation(it.observationId)
            }
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