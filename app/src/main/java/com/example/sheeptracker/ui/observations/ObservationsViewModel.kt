package com.example.sheeptracker.ui.observations

import android.app.Application
import androidx.lifecycle.*
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

    val showEmptyListTextView = MediatorLiveData<Boolean>().apply {
        addSource(filter) {
            value = observations.value.isNullOrEmpty() && (it == null)
        }
        addSource(observations) {
            value = it.isNullOrEmpty() && (filter.value == null)
        }
    }

    val showNoHerdObservationsText = MediatorLiveData<Boolean>().apply {
        addSource(filter) {
            value = observations.value?.filter { obs -> obs.observationType == Observation.ObservationType.COUNT }.isNullOrEmpty() && (if (it == null) false else it == Observation.ObservationType.COUNT)
        }
        addSource(observations) {
            value = it.filter { obs -> obs.observationType == Observation.ObservationType.COUNT }.isNullOrEmpty() && (if (filter.value == null) false else filter.value == Observation.ObservationType.COUNT)
        }
    }

    val showNoDeadObservationsText = MediatorLiveData<Boolean>().apply {
        addSource(filter) {
            value = observations.value?.filter { obs -> obs.observationType == Observation.ObservationType.DEAD }.isNullOrEmpty() && (if (it == null) false else it == Observation.ObservationType.DEAD)
        }
        addSource(observations) {
            value = it.filter { obs -> obs.observationType == Observation.ObservationType.DEAD }.isNullOrEmpty() && (if (filter.value == null) false else filter.value == Observation.ObservationType.DEAD)
        }
    }

    val showNoInjuredObservationsText = MediatorLiveData<Boolean>().apply {
        addSource(filter) {
            value = observations.value?.filter { obs -> obs.observationType == Observation.ObservationType.INJURED }.isNullOrEmpty() && (if (it == null) false else it == Observation.ObservationType.INJURED)
        }
        addSource(observations) {
            value = it.filter { obs -> obs.observationType == Observation.ObservationType.INJURED }.isNullOrEmpty() && (if (filter.value == null) false else filter.value == Observation.ObservationType.INJURED)
        }
    }

    val showObservationsRV = MediatorLiveData<Boolean>().apply {
        addSource(showEmptyListTextView) {
            value = !it && !(showNoHerdObservationsText.value?: true) && !(showNoDeadObservationsText.value?: true) && !(showNoInjuredObservationsText.value?: true)
        }
        addSource(showNoHerdObservationsText) {
            value = !it && !(showEmptyListTextView.value?: true) && !(showNoDeadObservationsText.value?: true) && !(showNoInjuredObservationsText.value?: true)
        }
        addSource(showNoDeadObservationsText) {
            value = !it && !(showNoHerdObservationsText.value?: true) && !(showEmptyListTextView.value?: true) && !(showNoInjuredObservationsText.value?: true)
        }
        addSource(showNoInjuredObservationsText) {
            value = !it && !(showNoHerdObservationsText.value?: true) && !(showNoDeadObservationsText.value?: true) && !(showEmptyListTextView.value?: true)
        }
    }

}