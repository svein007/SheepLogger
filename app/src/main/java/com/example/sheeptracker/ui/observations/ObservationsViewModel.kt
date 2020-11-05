package com.example.sheeptracker.ui.observations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.Trip

class ObservationsViewModel(
    tripId: Long,
    appDao: AppDao,
    application: Application
) : AndroidViewModel(application) {

    var filter = MutableLiveData<Observation.ObservationType?>(null)

    val trip: LiveData<Trip?> = appDao.getTripLD(tripId)
    val observations: LiveData<List<Observation>> = appDao.getObservationsForTripLDDesc(tripId)

    val showEmptyListTextView = Transformations.map(observations) {
        it.isNullOrEmpty()
    }

}