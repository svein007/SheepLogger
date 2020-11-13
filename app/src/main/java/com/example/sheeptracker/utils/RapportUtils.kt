package com.example.sheeptracker.utils

import android.content.Context
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

suspend fun generateSimpleRapport(context: Context, year: Int = -1): String {

    val appDao = AppDatabase.getInstance(context).appDatabaseDao

    return withContext(Dispatchers.IO){
        val injuredAnimalObservations = appDao.getInjuredAnimals().filter {
            Calendar.getInstance().apply{
                time = it.observationDate
            }.get(Calendar.YEAR) == year || year < 0
        }
        val injuredAnimalNumbers = injuredAnimalObservations.map { obs ->
            appDao.getAnimalRegistrationForObservation(obs.observationId)?.animalNumber ?: ""
        }.filter { animalNumber -> animalNumber.isNotBlank() }.map { num -> "#$num" }
        val injuredAnimalCount = injuredAnimalObservations.count()
        val injuredAnimalsString = "$injuredAnimalCount" + if (injuredAnimalNumbers.isNullOrEmpty()) "" else injuredAnimalNumbers.joinToString(separator = ", ", prefix = "\n    ")

        val deadAnimalObservations = appDao.getDeadAnimals().filter {
            Calendar.getInstance().apply{
                time = it.observationDate
            }.get(Calendar.YEAR) == year || year < 0
        }
        val deadAnimalNumbers = deadAnimalObservations.map { obs ->
            appDao.getAnimalRegistrationForObservation(obs.observationId)?.animalNumber ?: ""
        }.filter { animalNumber -> animalNumber.isNotBlank() }.map { num -> "#$num" }
        val deadAnimalCount = deadAnimalObservations.count()
        val deadAnimalString = "$deadAnimalCount" + if (deadAnimalNumbers.isNullOrEmpty()) "" else deadAnimalNumbers.joinToString(separator = ", ", prefix = "\n    ")

        val tripCount = appDao.getTrips().filter {
            Calendar.getInstance().apply{
                time = it.tripDate
            }.get(Calendar.YEAR) == year || year < 0
        }.count()

        val totalKm = appDao.getFinishedTripsAsc().filter {
            Calendar.getInstance().apply{
                time = it.tripDate
            }.get(Calendar.YEAR) == year || year < 0
        }.fold(0.0) { acc, trip ->
            acc + getTotalDistance(appDao.getTripMapPointsForTrip(trip.tripId))
        }

        val tripDuration = appDao.getFinishedTripsAsc().filter {
            Calendar.getInstance().apply{
                time = it.tripDate
            }.get(Calendar.YEAR) == year || year < 0
        }.fold(0L) { acc, trip ->
            acc + (trip.tripFinishedDate!!.time - trip.tripDate.time)
        }

        val tripDurationString = durationString(tripDuration)

        context.getString(
            R.string.rapport_text,
            deadAnimalString,
            injuredAnimalsString,
            tripCount,
            totalKm,
            tripDurationString
        )
    }

}