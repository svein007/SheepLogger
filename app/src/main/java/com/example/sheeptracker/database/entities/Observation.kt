package com.example.sheeptracker.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "observation_table",
        foreignKeys = [
            ForeignKey(
                entity = Trip::class,
                parentColumns = arrayOf("trip_id"),
                childColumns = arrayOf("observation_owner_trip_id"),
                onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                entity = TripMapPoint::class,
                parentColumns = arrayOf("trip_map_point_id"),
                childColumns = arrayOf("observation_owner_trip_map_point_id"),
                onDelete = ForeignKey.CASCADE
            )
        ])
data class Observation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "observation_id")
    var observationId: Long = 0L,

    @ColumnInfo(name = "observation_note")
    var observationNote: String,

    @ColumnInfo(name = "observation_lat")
    var observationLat: Double,

    @ColumnInfo(name = "observation_lon")
    var observationLon: Double,

    @ColumnInfo(name = "observation_date_time")
    var observationDate: Date,

    @ColumnInfo(name = "observation_owner_trip_map_point_id")
    var observationOwnerTripMapPointId: Long,

    @ColumnInfo(name = "observation_owner_trip_id")
    var observationOwnerTripId: Long

) {

}