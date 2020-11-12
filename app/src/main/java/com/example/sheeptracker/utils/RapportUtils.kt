package com.example.sheeptracker.utils

import android.content.Context
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

suspend fun generateSimpleRapport(context: Context): String {

    val appDao = AppDatabase.getInstance(context).appDatabaseDao

    return withContext(Dispatchers.IO){
        val injuredAnimals = appDao.getInjuredAnimalCount()
        val deadAnimals = appDao.getDeadAnimalCount()
        val tripCount = appDao.getTripCount()

        val totalKm = appDao.getFinishedTripsAsc().fold(0.0) { acc, trip ->
            acc + getTotalDistance(appDao.getTripMapPointsForTrip(trip.tripId))
        }

        val tripDuration = appDao.getFinishedTripsAsc().fold(0L) { acc, trip ->
            acc + (trip.tripFinishedDate!!.time - trip.tripDate.time)
        }

        val tripDurationString = durationString(tripDuration)

        context.getString(
            R.string.rapport_text,
            deadAnimals,
            injuredAnimals,
            tripCount,
            totalKm,
            tripDurationString
        )
    }

}

suspend fun generateSimpleRapport(context: Context, year: Int): String {

    val appDao = AppDatabase.getInstance(context).appDatabaseDao

    return withContext(Dispatchers.IO){
        val injuredAnimals = appDao.getInjuredAnimals().filter {
            Calendar.getInstance().apply{
                time = it.observationDate
            }.get(Calendar.YEAR) == year
        }.count()

        val deadAnimals = appDao.getDeadAnimals().filter {
            Calendar.getInstance().apply{
                time = it.observationDate
            }.get(Calendar.YEAR) == year
        }.count()

        val tripCount = appDao.getTrips().filter {
            Calendar.getInstance().apply{
                time = it.tripDate
            }.get(Calendar.YEAR) == year
        }.count()

        val totalKm = appDao.getFinishedTripsAsc().filter {
            Calendar.getInstance().apply{
                time = it.tripDate
            }.get(Calendar.YEAR) == year
        }.fold(0.0) { acc, trip ->
            acc + getTotalDistance(appDao.getTripMapPointsForTrip(trip.tripId))
        }

        val tripDuration = appDao.getFinishedTripsAsc().filter {
            Calendar.getInstance().apply{
                time = it.tripDate
            }.get(Calendar.YEAR) == year
        }.fold(0L) { acc, trip ->
            acc + (trip.tripFinishedDate!!.time - trip.tripDate.time)
        }

        val tripDurationString = durationString(tripDuration)

        context.getString(
            R.string.rapport_text,
            deadAnimals,
            injuredAnimals,
            tripCount,
            totalKm,
            tripDurationString
        )
    }

}