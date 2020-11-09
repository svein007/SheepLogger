package com.example.sheeptracker.ui.tripdetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.TripMapPoint
import com.example.sheeptracker.utils.getTotalDistance
import kotlin.math.roundToInt

class TripDetailsViewModel(
    private val tripId: Long,
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    val trip = appDao.getTripLD(tripId)
    val mapArea: LiveData<MapArea?> = appDao.getMapAreaForTripLD(tripId)
    val observations: LiveData<List<Observation>> = appDao.getObservationsForTripLDAsc(tripId)
    val observationCount: LiveData<Int> = appDao.getObservationCountForTripLD(tripId)
    val tripMapPoints: LiveData<List<TripMapPoint>> = appDao.getTripMapPointsForTripLD(tripId)
    val deadAnimalCount = appDao.getDeadAnimalCountForTripLD(tripId)
    val injuredAnimalCount = appDao.getInjuredAnimalCountForTripLD(tripId)

    val tripDistance = Transformations.map(tripMapPoints) {
        var distance = 0.0
        tripMapPoints.value?.let {
            distance = getTotalDistance(it).roundToInt() / 1000.0
        }
        return@map "${distance}km"
    }

}