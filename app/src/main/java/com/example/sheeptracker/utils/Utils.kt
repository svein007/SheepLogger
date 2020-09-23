package com.example.sheeptracker.utils

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import androidx.core.net.toFile
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.entities.TripMapPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/** Gets observation-position to be associated with the observation **/
suspend fun getObservedFromPoint(appDao: AppDao, tripId: Long, currentPosition: TripMapPoint): TripMapPoint {
    return withContext(Dispatchers.IO) {
        val currentLastPoint = appDao.getTripMapPointsForTrip(tripId).maxByOrNull { point -> point.tripMapPointId }

        var currentToLastDistance = -1.0

        if (currentLastPoint != null)
            currentToLastDistance = GeoPoint(currentLastPoint.tripMapPointLat, currentLastPoint.tripMapPointLon).distanceToAsDouble(
                GeoPoint(currentPosition.tripMapPointLat, currentPosition.tripMapPointLon)
            )

        if (currentToLastDistance > 5.0 || currentLastPoint == null) {
            val id = appDao.insert(currentPosition)
            return@withContext appDao.getTripMapPoint(id)!!
        }

        return@withContext currentLastPoint
    }
}

fun getDrawableFromUri(context: Context, imgUri: Uri): Drawable? {
    return try {
        val inputStream = context.contentResolver.openInputStream(imgUri)
        Drawable.createFromStream(inputStream, imgUri.toString() )
    } catch (e:Exception) {
        null
    }
}

fun storeDrawableWithName(context: Context, drawable: Drawable, fileName: String): String {
    val bitmap = (drawable as BitmapDrawable).bitmap
    val wrapper = ContextWrapper(context.applicationContext)
    var file = wrapper.getDir("images", Context.MODE_PRIVATE)
    file = File(file, "$fileName.jpg")

    val fileUriStr = Uri.fromFile(file).toString()

    try {
        val stream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
    } catch (e: IOException){ // Catch the exception
        e.printStackTrace()
    }

    return fileUriStr
}

fun deleteFile(fileUri: Uri) {
    val file = fileUri.toFile()
    if(file.exists()) {
        file.delete()
    }
}

@Throws(IOException::class)
fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}