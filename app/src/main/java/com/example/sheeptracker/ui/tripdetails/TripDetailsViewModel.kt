package com.example.sheeptracker.ui.tripdetails

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.TripMapPoint
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

    private fun getTotalDistance(tripMapPoints: List<TripMapPoint>): Double {

        if (tripMapPoints.size < 2) return 0.0

        var distance = 0.0
        for (i in 0 .. (tripMapPoints.size-2)) {
            distance += distanceBetween(tripMapPoints[i], tripMapPoints[i+1])
        }

        return distance
    }

    private fun distanceBetween(p1: TripMapPoint, p2: TripMapPoint): Double {
        val distance = FloatArray(1)
        Location.distanceBetween(p1.tripMapPointLat, p1.tripMapPointLon, p2.tripMapPointLat, p2.tripMapPointLon, distance)
        return distance[0].toDouble()
    }

}