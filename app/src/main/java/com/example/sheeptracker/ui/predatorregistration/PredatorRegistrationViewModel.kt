package com.example.sheeptracker.ui.predatorregistration

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.TripMapPoint
import com.example.sheeptracker.map.MapAreaManager
import com.example.sheeptracker.utils.addImgResDrawableToDB
import com.example.sheeptracker.utils.addImgResUriToDB
import com.example.sheeptracker.utils.deleteFile
import com.example.sheeptracker.utils.getObservedFromPoint
import kotlinx.coroutines.*
import java.util.*

class PredatorRegistrationViewModel(
    private val observationId: Long,
    private val tripId: Long,
    private val obsLat: Double,
    private val obsLon: Double,
    private val obsType: Int,
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val obsIdLD = MutableLiveData(observationId)

    val observation: LiveData<Observation?> = Transformations.switchMap(obsIdLD) {
        appDao.getObservationLD(it)
    }

    val imageResources = Transformations.switchMap(obsIdLD) {
        appDao.getImageResourcesLD(it)
    }

    val showEmptyImageListTextView = Transformations.map(imageResources) {
        it.isNullOrEmpty()
    }

    init {
        uiScope.launch {
            val newObs = observationId < 0
            if (newObs) {
                MapAreaManager.getLastKnownLocation(
                    getApplication<Application>().applicationContext,
                    null,
                    1
                )?.let {
                    val currentPosition = TripMapPoint(
                        tripMapPointLon =  it.longitude,
                        tripMapPointLat = it.latitude ,
                        tripMapPointDate = Date(),
                        tripMapPointOwnerTripId = tripId
                    )

                    val observationPoint = getObservedFromPoint(appDao, tripId, currentPosition)

                    val newObservation = Observation(
                        observationLat = obsLat,
                        observationLon = obsLon,
                        observationNote = "",
                        observationDate = Date(),
                        observationOwnerTripId = tripId,
                        observationOwnerTripMapPointId = observationPoint.tripMapPointId,
                        observationType = Observation.ObservationType.values()[obsType]
                    )

                    obsIdLD.value = insert(newObservation)
                }
            }
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
            obsIdLD.value?.let {
                addImgResUriToDB(
                    getApplication<Application>().applicationContext,
                    imgUri,
                    it,
                    appDao
                )
            }
        }
    }

    fun addImageResource(drawable: Drawable) {
        uiScope.launch {
            obsIdLD.value?.let {
                addImgResDrawableToDB(
                    getApplication<Application>().applicationContext,
                    drawable,
                    appDao,
                    it
                )
            }
        }
    }

    /** Helpers **/

    private suspend fun insert(observation: Observation): Long {
        return withContext(Dispatchers.IO) {
            appDao.insert(observation)
        }
    }

    private suspend fun update() {
        withContext(Dispatchers.IO){
            observation.value?.let {
                appDao.update(it)
            }
        }
    }


    private suspend fun delete() {
        withContext(Dispatchers.IO) {
            imageResources.value?.let {
                for (imgRes in it) {
                    deleteFile(imgRes.getImgUri())
                }
            }

            observation.value?.let {
                appDao.deleteObservation(it.observationId)
            }
        }
    }

}