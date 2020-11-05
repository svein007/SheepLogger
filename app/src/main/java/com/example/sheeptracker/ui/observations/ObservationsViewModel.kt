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
        it.isNullOrEmpty() && (filter.value == null)
    }

    val showNoHerdObservationsText = Transformations.map(observations) {
        it.filter { obs -> obs.observationType == Observation.ObservationType.COUNT }.isNullOrEmpty() && (if (filter.value == null) false else filter.value == Observation.ObservationType.COUNT)
    }

    val showNoDeadObservationsText = Transformations.map(observations) {
        it.filter { obs -> obs.observationType == Observation.ObservationType.DEAD }.isNullOrEmpty() && (if (filter.value == null) false else filter.value == Observation.ObservationType.DEAD)
    }

    val showNoInjuredObservationsText = Transformations.map(observations) {
        it.filter { obs -> obs.observationType == Observation.ObservationType.INJURED }.isNullOrEmpty() && (if (filter.value == null) false else filter.value == Observation.ObservationType.INJURED)
    }

}