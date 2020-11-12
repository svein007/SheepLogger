package com.example.sheeptracker.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Counter
import com.example.sheeptracker.database.entities.ImageResource
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.TripMapPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

suspend fun getObservationShortDesc(appDao: AppDao, context: Context, observation: Observation): String {
    return when(observation.observationType) {
        Observation.ObservationType.COUNT -> getCountersDesc(appDao, context, observation)
        Observation.ObservationType.DEAD -> {
            val deadString = context.getString(R.string.dead)
            "$deadString #${getAnimalRegisterNumber(appDao, observation)}"
        }
        Observation.ObservationType.INJURED -> {
            val injuredString = context.getString(R.string.injured)
            "$injuredString #${getAnimalRegisterNumber(appDao, observation)}"
        }
        Observation.ObservationType.PREDATOR -> {
            val predatorString = context.getString(R.string.predator)
            "$predatorString: ${observation.observationNote.substring(0, minOf(observation.observationNote.length, 10))}"
        }
        Observation.ObservationType.ENVIRONMENT -> {
            val environmentString = context.getString(R.string.environment)
            "$environmentString: ${observation.observationNote.substring(0, minOf(observation.observationNote.length, 10))}"
        }
    }
}


/**
 * Creates and stores a copy of the given file, and creates a ImageResource entry in DB.
 */
suspend fun addImgResUriToDB(
    context: Context, imgUri: String, obsId: Long, dao: AppDao
) {
    withContext(Dispatchers.IO) {
        val drawable = getDrawableFromUri(context, Uri.parse(imgUri))
        val newImgRes = ImageResource(imageResourceObservationId = obsId)
        val newId = dao.insert(newImgRes)
        val uriString = storeDrawableWithName(context, drawable!!, "img_${obsId}_$newId")
        val imgRes = dao.getImageResource(newId)

        imgRes.imageResourceUri = uriString

        dao.update(imgRes)
    }
}

suspend fun addImgResDrawableToDB(
    context: Context,
    drawable: Drawable,
    dao: AppDao,
    obsId: Long
) {
    withContext(Dispatchers.IO) {
        val newImgRes = ImageResource(imageResourceObservationId = obsId)
        val newId = dao.insert(newImgRes)
        val uriString = storeDrawableWithName(context, drawable, "img_${obsId}_$newId")
        val imgRes = dao.getImageResource(newId)

        imgRes.imageResourceUri = uriString

        dao.update(imgRes)
    }
}

suspend fun getCountersDesc(appDao: AppDao, context: Context, observation: Observation): String {
    return withContext(Dispatchers.IO) {
        val sheepCount = appDao.getCounter(observation.observationId, Counter.CountType.SHEEP)?.counterValue
        val lambCount = appDao.getCounter(observation.observationId, Counter.CountType.LAMB)?.counterValue
        val sheepString = context.getString(R.string.sheep)
        val lambString = context.getString(R.string.lamb)

        "$sheepCount $sheepString, \n${lambCount} $lambString"
    }
}

suspend fun getAnimalRegisterNumber(appDao: AppDao, observation: Observation): String {
    return withContext(Dispatchers.IO) {
        appDao.getAnimalRegistrationForObservation(observation.observationId)?.let {
            return@withContext it.animalNumber
        }
        return@withContext ""
    }
}

fun getTotalDistance(tripMapPoints: List<TripMapPoint>): Double {

    if (tripMapPoints.size < 2) return 0.0

    var distance = 0.0
    for (i in 0 .. (tripMapPoints.size-2)) {
        distance += distanceBetween(tripMapPoints[i], tripMapPoints[i+1])
    }

    return distance
}

fun distanceBetween(p1: TripMapPoint, p2: TripMapPoint): Double {
    val distance = FloatArray(1)
    Location.distanceBetween(p1.tripMapPointLat, p1.tripMapPointLon, p2.tripMapPointLat, p2.tripMapPointLon, distance)
    return distance[0].toDouble()
}

fun durationString(millis: Long): String {
    var res = ""
    val days = TimeUnit.MILLISECONDS.toDays(millis)
    val hours = TimeUnit.MILLISECONDS.toHours(millis - TimeUnit.DAYS.toMillis(days))
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours))
    if (days != 0L) {
        res += "${days}d"
    }
    res += " ${hours}h ${minutes}m"
    return res
}
