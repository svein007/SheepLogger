package com.example.sheeptracker.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.sheeptracker.utils.durationString
import java.util.*
import java.util.concurrent.TimeUnit

@Entity(tableName = "trip_table",
        foreignKeys = [
            ForeignKey(
                entity = MapArea::class,
                parentColumns = arrayOf("map_area_id"),
                childColumns = arrayOf("trip_owner_map_area_id"),
                onDelete = ForeignKey.CASCADE)
        ])
data class Trip (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trip_id")
    var tripId: Long = 0L,

    @ColumnInfo(name = "trip_name")
    var tripName: String,

    @ColumnInfo(name="trip_date")
    var tripDate: Date,

    @ColumnInfo(name="trip_finished")
    var tripFinished: Boolean = false,

    @ColumnInfo(name="trip_finished_date")
    var tripFinishedDate: Date? = null,

    @ColumnInfo(name = "trip_owner_map_area_id")
    var tripOwnerMapAreaId: Long

) {

    val tripDurationString: String
    get() {
        if (tripFinishedDate != null) {
            val diff = tripFinishedDate!!.time - tripDate.time
            return durationString(diff)
        }
        return ""
    }

}