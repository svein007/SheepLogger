package com.example.osmdroidexample.database

import androidx.lifecycle.LiveData
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

    @Query("SELECT * FROM map_area_table WHERE map_area_name = :mapName")
    fun getMapArea(mapName: String): MapArea?

    @Query("SELECT * FROM map_area_table WHERE map_area_name = :mapName")
    fun getMapAreaLD(mapName: String): LiveData<MapArea?>

    @Query("SELECT * FROM map_area_table ORDER BY map_area_name ASC")
    fun getMapAreas(): List<MapArea>

    @Query("SELECT * FROM map_area_table ORDER BY map_area_name ASC")
    fun getMapAreasLD(): LiveData<List<MapArea>>


}