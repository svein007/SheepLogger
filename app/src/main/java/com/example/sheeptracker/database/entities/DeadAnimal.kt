package com.example.sheeptracker.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dead_animal_table")
data class DeadAnimal(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "dead_animal_id")
    var deadAnimalId: Long = 0L,

    @ColumnInfo(name = "dead_animal_sheep_number")
    var deadAnimalSheepNumber: String = "",

    @ColumnInfo(name = "dead_animal_note")
    var deadAnimalNote: String = "",

    @ColumnInfo(name = "dead_animal_owner_observation_id")
    var deadAnimalOwnerObservationId: Long

) {
}