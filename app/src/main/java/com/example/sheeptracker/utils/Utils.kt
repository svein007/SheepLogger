package com.example.sheeptracker.utils

import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.TripMapPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint

/** Gets observation-position to be associated with the observation **/
suspend fun getObservedFromPoint(appDao: AppDao, tripId: Long, currentPosition: TripMapPoint): TripMapPoint {
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