package com.example.sheeptracker.ui.swiper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sheeptracker.database.entities.Counter
import com.example.sheeptracker.database.entities.Observation

abstract class SwiperViewModel(
    application: Application) : AndroidViewModel(application) {

    abstract val observation: LiveData<Observation?>
    abstract val countType: MutableLiveData<Counter.CountType>
    abstract val counters: LiveData<List<Counter>>

}