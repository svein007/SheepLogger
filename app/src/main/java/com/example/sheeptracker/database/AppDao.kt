package com.example.sheeptracker.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sheeptracker.database.entities.*
import java.util.*

@Dao
interface AppDao {

    /** MapArea **/

    @Insert
    fun insert(mapArea: MapArea): Long

    @Query("SELECT * FROM map_area_table WHERE map_area_id = :key")
    fun getMapArea(key: Long): MapArea?

    @Query("SELECT * FROM map_area_table WHERE map_area_id = :key")
    fun getMapAreaLD(key: Long): LiveData<MapArea?>

    @Query("SELECT map_area_id, map_area_name, map_area_min_zoom, map_area_max_zoom, map_area_bounding_box FROM map_area_table INNER JOIN trip_table ON map_area_id = trip_owner_map_area_id WHERE trip_id = :tripId")
    fun getMapAreaForTripLD(tripId: Long): LiveData<MapArea?>

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

    @Query("SELECT map_area_name FROM map_area_table ORDER BY map_area_name ASC")
    fun getMapAreaNames(): List<String>

    @Query("SELECT COUNT(*) FROM map_area_table")
    fun getMapAreaCount(): Int

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

    @Query("SELECT * FROM trip_table WHERE trip_finished = 1 ORDER BY trip_date ASC")
    fun getFinishedTripsLDAsc(): LiveData<List<Trip>>

    @Query("SELECT * FROM trip_table WHERE trip_finished = 1 ORDER BY trip_date ASC")
    fun getFinishedTripsAsc(): List<Trip>

    @Query("SELECT * FROM trip_table WHERE trip_finished = 1 ORDER BY trip_date DESC")
    fun getFinishedTripsLDDesc(): LiveData<List<Trip>>

    @Query("SELECT * FROM trip_table WHERE trip_owner_map_area_id = :mapAreaId ORDER BY trip_id ASC ")
    fun getTripsForMapArea(mapAreaId: Long): List<Trip>

    @Query("SELECT trip_id, trip_name, trip_date, trip_finished, trip_owner_map_area_id FROM trip_table INNER JOIN observation_table ON trip_id = observation_owner_trip_id WHERE observation_id = :observationId")
    fun getTripForObservation(observationId: Long): LiveData<Trip?>

    @Query("DELETE FROM trip_table WHERE trip_id = :key")
    fun deleteTrip(key: Long)

    @Query("SELECT * FROM trip_table WHERE trip_finished = 0 ORDER BY trip_date DESC LIMIT 1")
    fun getActiveTrip(): LiveData<Trip?>

    @Query("UPDATE trip_table SET trip_finished = 1, trip_finished_date = :finishDate WHERE trip_id = :key")
    fun finishTrip(key: Long, finishDate: Date)

    @Query("SELECT trip_finished FROM trip_table WHERE trip_id = :key")
    fun isTripFinished(key: Long): Boolean?

    @Query("SELECT trip_finished FROM trip_table WHERE trip_id = :key")
    fun isTripFinishedLD(key: Long): LiveData<Boolean?>

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
    fun getObservationsForTripLDAsc(tripId: Long): LiveData<List<Observation>>

    @Query("SELECT * FROM observation_table WHERE observation_owner_trip_id = :tripId ORDER BY observation_id DESC")
    fun getObservationsForTripLDDesc(tripId: Long): LiveData<List<Observation>>

    @Query("SELECT * FROM observation_table WHERE observation_id = :key")
    fun getObservationLD(key: Long): LiveData<Observation?>

    @Query("SELECT * FROM observation_table WHERE observation_id = :key")
    fun getObservation(key: Long): Observation?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(observation: Observation)

    @Query("DELETE FROM observation_table WHERE observation_id = :key")
    fun deleteObservation(key: Long)

    @Query("SELECT COUNT(*) FROM observation_table WHERE observation_owner_trip_id = :tripId")
    fun getObservationCountForTrip(tripId: Long): Int

    @Query("SELECT COUNT(*) FROM observation_table WHERE observation_owner_trip_id = :tripId")
    fun getObservationCountForTripLD(tripId: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM observation_table WHERE observation_owner_trip_id = (SELECT trip_id FROM trip_table WHERE trip_finished = 0 ORDER BY trip_date DESC LIMIT 1)")
    fun getObservationCountForActiveTrip(): LiveData<Int>

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

    @Query("SELECT * FROM animal_registration_table WHERE animal_registration_owner_observation_id = :observationId")
    fun getAnimalRegistrationForObservationLD(observationId: Long): LiveData<AnimalRegistration?>

    @Query("SELECT * FROM animal_registration_table WHERE animal_registration_id = :key")
    fun getAnimalRegistration(key: Long): AnimalRegistration?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(animalRegistration: AnimalRegistration)

    @Query("SELECT animal_registration_id FROM animal_registration_table WHERE animal_registration_owner_observation_id = :observationId")
    fun getAnimalRegistrationId(observationId: Long): Long

    @Query("DELETE FROM animal_registration_table WHERE animal_registration_id = :key")
    fun deleteAnimalRegistration(key: Long)

    @Query("SELECT COUNT(*) FROM observation_table INNER JOIN trip_table ON trip_id = observation_owner_trip_id WHERE observation_type = 2 AND trip_id = :tripId")
    fun getInjuredAnimalCountForTripLD(tripId: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM observation_table INNER JOIN trip_table ON trip_id = observation_owner_trip_id WHERE observation_type = 1 AND trip_id = :tripId")
    fun getDeadAnimalCountForTripLD(tripId: Long): LiveData<Int>

    @Query("SELECT * FROM observation_table WHERE observation_type = 2")
    fun getInjuredAnimals(): List<Observation>

    @Query("SELECT * FROM observation_table WHERE observation_type = 1")
    fun getDeadAnimals(): List<Observation>

    /** ImageResource **/

    @Insert
    fun insert(imageResource: ImageResource): Long

    @Query("SELECT * FROM image_resource_table WHERE image_resource_observation_id = :observationId")
    fun getImageResourcesLD(observationId: Long): LiveData<List<ImageResource>>

    @Query("SELECT * FROM image_resource_table WHERE image_resource_observation_id = :observationId")
    fun getImageResources(observationId: Long): List<ImageResource>

    @Query("SELECT * FROM image_resource_table WHERE image_resource_id = :key")
    fun getImageResource(key: Long): ImageResource

    @Query("SELECT * FROM image_resource_table WHERE image_resource_id = :key")
    fun getImageResourceLD(key: Long): LiveData<ImageResource>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(imageResource: ImageResource)

    @Query("DELETE FROM image_resource_table WHERE image_resource_id = :key")
    fun deleteImageResource(key: Long)


    /** Util methods **/

    @Query("SELECT COUNT(*) FROM observation_table WHERE observation_type = 2")
    fun getInjuredAnimalCount(): Int

    @Query("SELECT COUNT(*) FROM observation_table WHERE observation_type = 1")
    fun getDeadAnimalCount(): Int

    @Query("SELECT COUNT(*) FROM trip_table")
    fun getTripCount(): Int

    @Query("SELECT COUNT(*) FROM trip_table WHERE trip_owner_map_area_id = :mapAreaId")
    fun getTripCount(mapAreaId: Long): Int

}