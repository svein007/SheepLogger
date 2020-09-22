package com.example.sheeptracker.ui.imageresource

import android.app.Application
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.utils.getDrawableFromUri

class ImageResourceViewModel(
    application: Application,
    private val appDao: AppDao,
    private val imageResourceId: Long,
    private val imageUri: String
) : AndroidViewModel(application) {

    val imageDrawable = MutableLiveData<Drawable>()

    init {
        imageDrawable.value = getDrawableFromUri(application.applicationContext, Uri.parse(imageUri))
    }

}