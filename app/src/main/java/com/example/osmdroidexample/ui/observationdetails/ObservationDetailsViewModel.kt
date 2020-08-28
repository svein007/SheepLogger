package com.example.osmdroidexample.ui.observationdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.entities.Observation

class ObservationDetailsViewModel(
    observationId: Long,
    appDao: AppDao) : ViewModel() {

    val observation: LiveData<Observation> = appDao.getObservationLD(observationId)

}