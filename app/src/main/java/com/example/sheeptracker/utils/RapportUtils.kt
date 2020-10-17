package com.example.sheeptracker.utils

import android.content.Context
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun generateSimpleRapport(context: Context): String {

    val appDao = AppDatabase.getInstance(context).appDatabaseDao

    return withContext(Dispatchers.IO){
        val injuredAnimals = appDao.getInjuredAnimalCount()
        val deadAnimals = appDao.getDeadAnimalCount()

        context.getString(R.string.simple_rapport_text, deadAnimals, injuredAnimals)
    }

}