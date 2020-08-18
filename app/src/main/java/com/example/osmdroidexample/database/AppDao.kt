package com.example.osmdroidexample.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.osmdroidexample.database.entities.MapArea

@Dao
interface AppDao {

    @Insert
    fun insert(mapArea: MapArea): Long

    @Query("SELECT * FROM map_area_table WHERE map_area_id = :key")
    fun getMapArea(key: Long): MapArea?

}