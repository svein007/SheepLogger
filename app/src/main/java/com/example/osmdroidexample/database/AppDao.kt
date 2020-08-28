package com.example.osmdroidexample.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.osmdroidexample.database.entities.MapArea
import com.example.osmdroidexample.database.entities.Observation
import com.example.osmdroidexample.database.entities.Trip

@Dao
interface AppDao {

    /** MapArea **/

    @Insert
    fun insert(mapArea: MapArea): Long

    @Query("SELECT * FROM map_area_table WHERE map_area_id = :key")
    fun getMapArea(key: Long): MapArea?

    @Query("SELECT * FROM map_area_table WHERE map_area_id = :key")
    fun getMapAreaLD(key: Long): LiveData<MapArea?>

    @Query("SELECT * FROM map_area_table WHERE map_area_name = :mapName")
    fun getMapArea(mapName: String): MapArea?

    @Query("SELECT * FROM map_area_table WHERE map_area_name = :mapName")
    fun getMapAreaLD(mapName: String): LiveData<MapArea?>

    @Query("SELECT * FROM map_area_table ORDER BY map_area_name ASC")
    fun getMapAreas(): List<MapArea>

    @Query("SELECT * FROM map_area_table ORDER BY map_area_name ASC")
    fun getMapAreasLD(): LiveData<List<MapArea>>

    @Query("DELETE FROM map_area_table WHERE map_area_id = :key")
    fun deleteMapArea(key: Long)


    /** Trip **/

    @Insert
    fun insert(trip: Trip): Long

    @Query("SELECT * FROM trip_table WHERE trip_id = :key")
    fun getTrip(key: Long): Trip?

    @Query("SELECT * FROM trip_table WHERE trip_id = :key")
    fun getTripLD(key: Long): LiveData<Trip?>

    @Query("SELECT * FROM trip_table ORDER BY trip_id ASC")
    fun getTrips(): List<Trip>

    @Query("SELECT * FROM trip_table ORDER BY trip_id ASC")
    fun getTripsLD(): LiveData<List<Trip>>

    @Query("SELECT * FROM trip_table WHERE trip_owner_map_area_id = :mapAreaId ORDER BY trip_id ASC ")
    fun getTripsForMapArea(mapAreaId: Long): List<Trip>


    /** Observation **/

    @Insert
    fun insert(observation: Observation): Long

    @Query("SELECT * FROM observation_table ORDER BY observation_id ASC")
    fun getObservationsLD(): LiveData<List<Observation>>

    @Query("SELECT * FROM observation_table WHERE observation_owner_trip_id = :tripId ORDER BY observation_id ASC")
    fun getObservationsForTripLD(tripId: Long): LiveData<List<Observation>>

    @Query("SELECT * FROM observation_table WHERE observation_id = :key")
    fun getObservationLD(key: Long): LiveData<Observation>


}