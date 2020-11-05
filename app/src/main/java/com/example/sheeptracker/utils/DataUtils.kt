package com.example.sheeptracker.utils

import android.content.Context
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Counter
import com.example.sheeptracker.database.entities.Observation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
