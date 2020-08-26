package com.example.osmdroidexample.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "trip_table")
data class Trip (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trip_id")
    var tripId: Long = 0L,

    @ColumnInfo(name = "trip_name")
    var tripName: String,

    @ColumnInfo(name="trip_date")
    var tripDate: String,

    @ForeignKey(entity = MapArea::class, parentColumns = ["map_area_id"], childColumns = ["trip_owner_map_area_id"], onDelete = ForeignKey.CASCADE)
    @ColumnInfo(name = "trip_owner_map_area_id")
    var tripOwnerMapAreaId: Long

) {

}