package com.example.osmdroidexample.ui.addobservation

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.Observation
import com.example.osmdroidexample.database.entities.Trip
import com.example.osmdroidexample.database.entities.TripMapPoint
import kotlinx.coroutines.*
import org.osmdroid.util.GeoPoint

class AddObservationViewModel(
    private val tripId: Long,
    private val currentPosition: TripMapPoint,
    application: Application,
    private val appDao: AppDao) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val trip: LiveData<Trip?> = appDao.getTripLD(tripId)

    val observationNote = MutableLiveData<String>()
    val observationSheepCount = MutableLiveData<Int>(0)
    val observationLambCount = MutableLiveData<Int>(0)

    /** Methods **/

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

    fun addObservation(lat: Double, lon: Double, onSuccess: () -> Unit, onFail: () -> Unit) {
        uiScope.launch {
            observationNote.value?.let {
                if (it.isBlank())
                    return@let

                try {
                    val observationPoint = getObservedFromPoint()

                    val newObservation = Observation(
                        observationLat = lat,
                        observationLon = lon,
                        observationSheepCount = observationSheepCount.value ?: 0,
                        observationLambCount = observationLambCount.value ?: 0,
                        observationNote = it,
                        observationOwnerTripId = tripId,
                        observationOwnerTripMapPointId = observationPoint.tripMapPointId
                    )

                    insert(newObservation)

                    onSuccess()
                } catch (e: SQLiteConstraintException) {
                    onFail()
                }

            }
        }
    }

    /** Helpers **/

    private suspend fun insert(observation: Observation): Long {
        return withContext(Dispatchers.IO) {
            appDao.insert(observation)
        }
    }

    /** Gets observation-position to be associated with the observation **/
    private suspend fun getObservedFromPoint(): TripMapPoint {
        return withContext(Dispatchers.IO) {
            val currentLastPoint = appDao.getTripMapPointsForTrip(tripId).maxByOrNull { point -> point.tripMapPointId }

            var currentToLastDistance = -1.0

            if (currentLastPoint != null)
                currentToLastDistance = GeoPoint(currentLastPoint.tripMapPointLat, currentLastPoint.tripMapPointLon).distanceToAsDouble(
                    GeoPoint(currentPosition.tripMapPointLat, currentPosition.tripMapPointLon)
                )

            if (currentToLastDistance > 5.0 || currentLastPoint == null) {
                val id = appDao.insert(currentPosition)
                return@withContext appDao.getTripMapPoint(id)!!
            }

            return@withContext currentLastPoint
        }
    }

}