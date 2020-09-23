package com.example.sheeptracker.ui.addanimalregistration

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.AnimalRegistration
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.Trip
import com.example.sheeptracker.database.entities.TripMapPoint
import com.example.sheeptracker.utils.getObservedFromPoint
import kotlinx.coroutines.*
import java.util.*

class AddAnimalRegistrationViewModel(
    private val tripId: Long,
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

    private val _deadAnimal = MutableLiveData<AnimalRegistration>()
    val animalRegistration: LiveData<AnimalRegistration>
        get() = _deadAnimal

    val observationTypeTitle = Transformations.map(observation) {
        when (observation.value!!.observationType) {
            Observation.ObservationType.DEAD -> "DEAD ANIMAL"
            Observation.ObservationType.INJURED -> "INJURED ANIMAL"
            else -> "-"
        }
    }

    init {
        val newObservation = Observation(
            observationLat = 0.0,
            observationLon = 0.0,
            observationNote = "",
            observationDate = observationDate,
            observationOwnerTripId = tripId,
            observationOwnerTripMapPointId = -1,
            observationType = observationType
        )

        _observation.value = newObservation

        val newDeadAnimal = AnimalRegistration(
            ownerObservationId = -1
        )
        _deadAnimal.value = newDeadAnimal
    }

    fun addObservation(lat: Double, lon: Double, onSuccess: () -> Unit, onFail: () -> Unit) {
        uiScope.launch {
            try {
                val observationPoint = getObservedFromPoint(appDao, tripId, currentPosition)

                observation.value?.apply {
                    observationLat = lat
                    observationLon = lon
                    observationOwnerTripMapPointId = observationPoint.tripMapPointId
                }

                val obsId = observation.value?.let { insert(it) }

                if (obsId != null && animalRegistration.value != null) {
                    _deadAnimal.value!!.ownerObservationId = obsId
                    insert(animalRegistration.value!!)
                }

                onSuccess()
            } catch (e: SQLiteConstraintException) {
                onFail()
            }
        }
    }

    /** Helpers **/

    private suspend fun insert(observation: Observation): Long {
        return withContext(Dispatchers.IO) {
            appDao.insert(observation)
        }
    }

    private suspend fun insert(animalRegistration: AnimalRegistration) {
        return withContext(Dispatchers.IO) {
            appDao.insert(animalRegistration)
        }
    }

}