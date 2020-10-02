package com.example.sheeptracker.ui.addanimalregistration

import android.app.Application
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.*
import com.example.sheeptracker.utils.deleteFile
import com.example.sheeptracker.utils.getDrawableFromUri
import com.example.sheeptracker.utils.getObservedFromPoint
import com.example.sheeptracker.utils.storeDrawableWithName
import kotlinx.coroutines.*
import java.util.*

class AddAnimalRegistrationViewModel(
    private val tripId: Long,
    private val obsLat: Double,
    private val obsLon: Double,
    private val currentPosition: TripMapPoint,
    private val observationType: Observation.ObservationType,
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val observationDate = Date()

    val trip: LiveData<Trip?> = appDao.getTripLD(tripId)

    private val _observation = MutableLiveData<Observation>()
    val observation: LiveData<Observation>
        get() = _observation

    private val _animalRegistration = MutableLiveData<AnimalRegistration>()
    val animalRegistration: LiveData<AnimalRegistration>
        get() = _animalRegistration

    private var _imageResources = MutableLiveData<List<ImageResource>>()
    val imageResources: LiveData<List<ImageResource>>
        get() = _imageResources

    val observationTypeTitle = Transformations.map(observation) {
        when (observation.value!!.observationType) {
            Observation.ObservationType.DEAD -> application.getString(R.string.dead_animal)
            Observation.ObservationType.INJURED -> application.getString(R.string.injured_animal)
            else -> "-"
        }
    }

    val showEmptyImageListTextView = Transformations.map(imageResources) {
        it.isNullOrEmpty()
    }

    init {

        uiScope.launch {
            val observationPoint = getObservedFromPoint(appDao, tripId, currentPosition)

            val newObservation = Observation(
                observationLat = obsLat,
                observationLon = obsLon,
                observationNote = "",
                observationDate = observationDate,
                observationOwnerTripId = tripId,
                observationOwnerTripMapPointId = observationPoint.tripMapPointId,
                observationType = observationType
            )

            val obsId = insert(newObservation)
            _observation.value = getObservation(obsId)

            val newAnimalRegistration = AnimalRegistration(
                ownerObservationId = _observation.value!!.observationId
            )
            val aniRegId = insert(newAnimalRegistration)
            _animalRegistration.value = getAnimalRegistration(aniRegId)

        }
    }

    fun deleteObservation() {
        uiScope.launch {
            delete()
        }
    }

    fun updateObservation() {
        uiScope.launch {
            update()
        }
    }

    fun addImageResource(imgUri: String) {
        uiScope.launch {
            addImgResToDB(imgUri)
            _imageResources.value = getImgResources()
        }
    }

    fun addImageResource(drawable: Drawable) {
        uiScope.launch {
            addImgResToDB(drawable)
            _imageResources.value = getImgResources()
        }
    }

    fun refreshImageResources() {
        uiScope.launch {
            _imageResources.value = getImgResources()
        }
    }

    /** Helpers **/

    private suspend fun insert(observation: Observation): Long {
        return withContext(Dispatchers.IO) {
            appDao.insert(observation)
        }
    }

    private suspend fun insert(animalRegistration: AnimalRegistration): Long {
        return withContext(Dispatchers.IO) {
            appDao.insert(animalRegistration)
        }
    }

    private suspend fun getObservation(key: Long): Observation? {
        return withContext(Dispatchers.IO) {
            return@withContext appDao.getObservation(key)
        }
    }

    private suspend fun getAnimalRegistration(key: Long): AnimalRegistration? {
        return withContext(Dispatchers.IO) {
            return@withContext appDao.getAnimalRegistration(key)
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

    private suspend fun update() {
        withContext(Dispatchers.IO){
            observation.value?.let {
                appDao.update(it)
            }
            animalRegistration.value?.let {
                appDao.update(it)
            }
        }
    }

    private suspend fun getImgResources(): List<ImageResource>? {
        return withContext(Dispatchers.IO){
            observation.value?.let {
                return@withContext appDao.getImageResources(it.observationId)
            }
            return@withContext null
        }
    }

    /**
     * Creates and stores a copy of the given file, and creates a ImageResource entry in DB.
     */
    private suspend fun addImgResToDB(imgUri: String) {
        withContext(Dispatchers.IO) {
            val drawable = getDrawableFromUri(getApplication<Application>().applicationContext, Uri.parse(imgUri))
            val newImgRes = ImageResource(imageResourceObservationId = observation.value!!.observationId)
            val newId = appDao.insert(newImgRes)
            val uriString = storeDrawableWithName(getApplication<Application>().applicationContext, drawable!!, "img_${observation.value!!.observationId}_$newId")
            val imgRes = appDao.getImageResource(newId)

            imgRes.imageResourceUri = uriString

            appDao.update(imgRes)
        }
    }

    private suspend fun addImgResToDB(drawable: Drawable) {
        withContext(Dispatchers.IO) {
            val newImgRes = ImageResource(imageResourceObservationId = observation.value!!.observationId)
            val newId = appDao.insert(newImgRes)
            val uriString = storeDrawableWithName(getApplication<Application>().applicationContext, drawable, "img_${observation.value!!.observationId}_$newId")
            val imgRes = appDao.getImageResource(newId)

            imgRes.imageResourceUri = uriString

            appDao.update(imgRes)
        }
    }

}