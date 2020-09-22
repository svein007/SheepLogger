package com.example.sheeptracker.ui.imageresource

import android.app.Application
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.utils.deleteFile
import com.example.sheeptracker.utils.getDrawableFromUri
import kotlinx.coroutines.*
import java.lang.Exception

class ImageResourceViewModel(
    application: Application,
    private val appDao: AppDao,
    private val imageResourceId: Long,
    private val imageUri: String
) : AndroidViewModel(application) {

    /** Private fields **/

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val imageDrawable = MutableLiveData<Drawable>()

    init {
        imageDrawable.value = getDrawableFromUri(application.applicationContext, Uri.parse(imageUri))
    }

    fun deleteImage() {
        uiScope.launch {
            deleteImgRes()
        }
    }

    /** Helpers **/

    private suspend fun deleteImgRes() {
        withContext(Dispatchers.IO) {
            Uri.parse(imageUri)?.let {
                try {
                    deleteFile(it)
                } catch (e: Exception) {}
            }
            appDao.deleteImageResource(imageResourceId)
        }
    }

}