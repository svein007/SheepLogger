package com.example.sheeptracker.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sheeptracker.database.entities.*

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

    @Query("DELETE FROM trip_table WHERE trip_id = :key")
    fun deletTrip(key: Long)


    /** TripMapPoint **/

    @Insert
    fun insert(tripMapPoint: TripMapPoint): Long

    @Query("SELECT * FROM trip_map_point_table WHERE trip_map_point_id = :key")
    fun getTripMapPoint(key: Long): TripMapPoint?

    @Query("SELECT * FROM trip_map_point_table WHERE trip_map_point_owner_trip_id = :tripId ORDER BY trip_map_point_date ASC ")
    fun getTripMapPointsForTrip(tripId: Long): List<TripMapPoint>

    @Query("SELECT * FROM trip_map_point_table WHERE trip_map_point_owner_trip_id = :tripId ORDER BY trip_map_point_date ASC ")
    fun getTripMapPointsForTripLD(tripId: Long): LiveData<List<TripMapPoint>>


    /** Observation **/

    @Insert
    fun insert(observation: Observation): Long

    @Query("SELECT * FROM observation_table ORDER BY observation_id ASC")
    fun getObservationsLD(): LiveData<List<Observation>>

    @Query("SELECT * FROM observation_table WHERE observation_owner_trip_id = :tripId ORDER BY observation_id ASC")
    fun getObservationsForTripLD(tripId: Long): LiveData<List<Observation>>

    @Query("SELECT * FROM observation_table WHERE observation_id = :key")
    fun getObservationLD(key: Long): LiveData<Observation>

    @Query("SELECT * FROM observation_table WHERE observation_id = :key")
    fun getObservation(key: Long): Observation?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(observation: Observation)

    @Query("DELETE FROM observation_table WHERE observation_id = :key")
    fun deleteObservation(key: Long)


    /** Counter **/

    @Insert
    fun insert(counter: Counter): Long

    @Query("SELECT * FROM counter_table WHERE counter_owner_observation_id = :observationId")
    fun getCounters(observationId: Long): List<Counter>

    @Query("SELECT * FROM counter_table WHERE counter_owner_observation_id = :observationId")
    fun getCountersLD(observationId: Long): LiveData<List<Counter>>

    @Query("SELECT * FROM counter_table WHERE counter_owner_observation_id = :observationId AND counter_type = :countType")
    fun getCounter(observationId: Long, countType: Counter.CountType): Counter?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(counter: Counter)

    @Query("DELETE FROM counter_table WHERE counter_id = :key")
    fun deleteCounter(key: Long)


    /** AnimalRegistration **/

    @Insert
    fun insert(animalRegistration: AnimalRegistration): Long

    @Query("SELECT * FROM animal_registration_table WHERE animal_registration_owner_observation_id = :observationId")
    fun getDeadAnimal(observationId: Long): LiveData<AnimalRegistration>

    @Query("SELECT * FROM animal_registration_table WHERE animal_registration_owner_observation_id = :observationId")
    fun getAnimalRegistrationForObservation(observationId: Long): AnimalRegistration?

    @Query("SELECT * FROM animal_registration_table WHERE animal_registration_id = :key")
    fun getAnimalRegistration(key: Long): AnimalRegistration?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(animalRegistration: AnimalRegistration)

    @Query("SELECT animal_registration_id FROM animal_registration_table WHERE animal_registration_owner_observation_id = :observationId")
    fun getAnimalRegistrationId(observationId: Long): Long

    @Query("DELETE FROM animal_registration_table WHERE animal_registration_id = :key")
    fun deleteAnimalRegistration(key: Long)


    /** ImageResource **/

    @Insert
    fun insert(imageResource: ImageResource): Long

    @Query("SELECT * FROM image_resource_table WHERE image_resource_observation_id = :observationId")
    fun getImageResourcesLD(observationId: Long): LiveData<List<ImageResource>>

    @Query("SELECT * FROM image_resource_table WHERE image_resource_observation_id = :observationId")
    fun getImageResources(observationId: Long): List<ImageResource>

    @Query("SELECT * FROM image_resource_table WHERE image_resource_id = :key")
    fun getImageResource(key: Long): ImageResource

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(imageResource: ImageResource)

    @Query("DELETE FROM image_resource_table WHERE image_resource_id = :key")
    fun deleteImageResource(key: Long)


}