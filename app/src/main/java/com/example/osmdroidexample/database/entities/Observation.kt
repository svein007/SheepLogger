package com.example.osmdroidexample.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "observation_table",
        foreignKeys = [
            ForeignKey(
                entity = Trip::class,
                parentColumns = arrayOf("trip_id"),
                childColumns = arrayOf("observation_owner_trip_id"),
                onDelete = ForeignKey.CASCADE
            )
        ])
data class Observation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "observation_id")
    var observationId: Long = 0L,

    @ColumnInfo(name = "observation_note")
    var observationNote: String,

    @ForeignKey(entity = Trip::class, parentColumns = ["trip_id"], childColumns = ["observation_owner_trip_id"], onDelete = ForeignKey.CASCADE)
    @ColumnInfo(name = "observation_owner_trip_id")
    var observationOwnerTripId: Long

) {

}