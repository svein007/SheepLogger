package com.example.sheeptracker.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "animal_registration_table",
    foreignKeys = [
    ForeignKey(
        entity = Observation::class,
        parentColumns = arrayOf("observation_id"),
        childColumns = arrayOf("animal_registration_owner_observation_id"),
        onDelete = ForeignKey.CASCADE)
    ])
data class AnimalRegistration(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "animal_registration_id")
    var id: Long = 0L,

    @ColumnInfo(name = "animal_registration_sheep_number")
    var animalNumber: String = "",

    @ColumnInfo(name = "animal_registration_note")
    var note: String = "",

    @ColumnInfo(name = "animal_registration_owner_observation_id")
    var ownerObservationId: Long

) {
}