package com.example.sheeptracker.ui.trips

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.Trip
import kotlinx.coroutines.*

class TripsViewModel(
    application: Application,
    private val appDao: AppDao
) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /** ViewModel fields **/

    val filter = MutableLiveData(Pair(1, getApplication<Application>().getString(R.string.all)))

    val trips: LiveData<List<Trip>> = appDao.getFinishedTripsLDDesc()

    val showEmptyListTextView = Transformations.map(trips) {
        it.isNullOrEmpty()
    }

}