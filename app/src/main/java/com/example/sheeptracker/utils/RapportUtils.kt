package com.example.sheeptracker.utils

import android.content.Context
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.database.entities.Observation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

suspend fun generateSimpleRapport(context: Context, year: Int = -1): String {

    val appDao = AppDatabase.getInstance(context).appDatabaseDao

    return withContext(Dispatchers.IO){
        val injuredAnimalNumbers = getAnimalRegistrationNumbers(appDao, appDao.getInjuredAnimals(), year).map { num -> "#$num" }
        val injuredAnimalCount = injuredAnimalNumbers.count()
        val injuredAnimalsString = "$injuredAnimalCount" + if (injuredAnimalNumbers.isNullOrEmpty()) "" else injuredAnimalNumbers.joinToString(separator = ", ", prefix = "\n    ")

        val deadAnimalNumbers = getAnimalRegistrationNumbers(appDao, appDao.getDeadAnimals(), year).map { num -> "#$num" }
        val deadAnimalCount = deadAnimalNumbers.count()
        val deadAnimalString = "$deadAnimalCount" + if (deadAnimalNumbers.isNullOrEmpty()) "" else deadAnimalNumbers.joinToString(separator = ", ", prefix = "\n    ")

        context.getString(
            R.string.rapport_text,
            deadAnimalString,
            injuredAnimalsString,
            getTripCount(appDao, year),
            getTotalDistance(appDao, year),
            getTotalDuration(appDao, year)
        )
    }

}

suspend fun getJSONRapport(context: Context, year: Int = -1): String {
    val appDao = AppDatabase.getInstance(context).appDatabaseDao
    return withContext(Dispatchers.IO) {
        val json = JSONObject()

        json.put("tripCount", getTripCount(appDao, year))
        json.put("totalDuration", getTotalDuration(appDao, year))
        json.put("totalDistance", getTotalDistance(appDao, year))

        val injuredAnimalNumbers = getAnimalRegistrationNumbers(appDao, appDao.getInjuredAnimals(), year)
        json.put("injuredCount", injuredAnimalNumbers.count())
        json.put("injuredAnimals", JSONArray(injuredAnimalNumbers))

        val deadAnimalNumbers = getAnimalRegistrationNumbers(appDao, appDao.getDeadAnimals(), year)
        json.put("deadCount", deadAnimalNumbers.count())
        json.put("deadAnimals", JSONArray(deadAnimalNumbers))

        json.toString()
    }
}

/** Helpers **/

private fun getTripCount(appDao: AppDao, year: Int = -1): Int {
    return appDao.getTrips().filter {
        Calendar.getInstance().apply {
            time = it.tripDate
        }.get(Calendar.YEAR) == year || year < 0
    }.count()
}

private fun getTotalDistance(appDao: AppDao, year: Int = -1): Double {
    return appDao.getFinishedTripsAsc().filter {
        Calendar.getInstance().apply{
            time = it.tripDate
        }.get(Calendar.YEAR) == year || year < 0
    }.fold(0.0) { acc, trip ->
        acc + getTotalDistance(appDao.getTripMapPointsForTrip(trip.tripId))
    }
}

private fun getTotalDuration(appDao: AppDao, year: Int): String {
    return durationString(appDao.getFinishedTripsAsc().filter {
        Calendar.getInstance().apply{
            time = it.tripDate
        }.get(Calendar.YEAR) == year || year < 0
    }.fold(0L) { acc, trip ->
        acc + (trip.tripFinishedDate!!.time - trip.tripDate.time)
    })
}

private fun getAnimalRegistrationNumbers(appDao: AppDao, observations: List<Observation>, year: Int = -1): List<String> {
    val animalRegistrations = observations.filter {
        Calendar.getInstance().apply{
            time = it.observationDate
        }.get(Calendar.YEAR) == year || year < 0
    }
    return animalRegistrations.map { obs ->
        appDao.getAnimalRegistrationForObservation(obs.observationId)?.animalNumber ?: ""
    }.filter { animalNumber -> animalNumber.isNotBlank() }
}