package com.example.sheeptracker.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "trip_map_point_table",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = arrayOf("trip_id"),
            childColumns = arrayOf("trip_map_point_owner_trip_id"),
            onDelete = ForeignKey.CASCADE
        )
    ])
data class TripMapPoint(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trip_map_point_id")
    var tripMapPointId: Long = 0L,

    @ColumnInfo(name = "trip_map_point_lon")
    var tripMapPointLon: Double,

    @ColumnInfo(name = "trip_map_point_lat")
    var tripMapPointLat: Double,

    @ColumnInfo(name="trip_map_point_date")
    var tripMapPointDate: String,

    @ColumnInfo(name = "trip_map_point_owner_trip_id")
    var tripMapPointOwnerTripId: Long

    ){

}