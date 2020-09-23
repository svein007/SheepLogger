package com.example.sheeptracker.database.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "image_resource_table",
    foreignKeys = [
        ForeignKey(
            entity = Observation::class,
            parentColumns = arrayOf("observation_id"),
            childColumns = arrayOf("image_resource_observation_id"),
            onDelete = ForeignKey.CASCADE)
    ])
data class ImageResource(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "image_resource_id")
    var imageResourceId: Long = 0L,

    @ColumnInfo(name = "image_resource_uri")
    var imageResourceUri: String = "",

    @ColumnInfo(name = "image_resource_observation_id")
    var imageResourceObservationId: Long

) {
    fun getImgUri(): Uri {
        return Uri.parse(imageResourceUri)
    }
}