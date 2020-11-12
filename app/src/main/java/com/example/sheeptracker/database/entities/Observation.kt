package com.example.sheeptracker.database.entities

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.sheeptracker.R
import java.util.*

@Entity(tableName = "observation_table",
        foreignKeys = [
            ForeignKey(
                entity = Trip::class,
                parentColumns = arrayOf("trip_id"),
                childColumns = arrayOf("observation_owner_trip_id"),
                onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                entity = TripMapPoint::class,
                parentColumns = arrayOf("trip_map_point_id"),
                childColumns = arrayOf("observation_owner_trip_map_point_id"),
                onDelete = ForeignKey.CASCADE
            )
        ])
data class Observation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "observation_id")
    var observationId: Long = 0L,

    @ColumnInfo(name = "observation_note")
    var observationNote: String,

    @ColumnInfo(name = "observation_lat")
    var observationLat: Double,

    @ColumnInfo(name = "observation_lon")
    var observationLon: Double,

    @ColumnInfo(name = "observation_date_time")
    var observationDate: Date,

    @ColumnInfo(name = "observation_type")
    val observationType: ObservationType,

    @ColumnInfo(name = "observation_owner_trip_map_point_id")
    var observationOwnerTripMapPointId: Long,

    @ColumnInfo(name = "observation_secondary_trip_map_point_id")
    var observationSecondaryTripMapPointId: Long? = null,

    @ColumnInfo(name = "observation_owner_trip_id")
    var observationOwnerTripId: Long

) {

    enum class ObservationType {
        COUNT, DEAD, INJURED, PREDATOR, ENVIRONMENT;

        fun getDrawable(resources: Resources): Drawable? {
            return when (this) {
                DEAD -> {
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_warning_red_24, null)
                }
                INJURED -> {
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_report_problem_24, null)
                }
                PREDATOR -> {
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_report_24, null)
                }
                COUNT -> {
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_remove_red_eye_24, null)
                }
                ENVIRONMENT -> {
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_nature_24, null)
                }
            }
        }

        fun getString(context: Context): String {
            return when (this) {
                DEAD -> {
                    context.getString(R.string.dead)
                }
                INJURED -> {
                    context.getString(R.string.injured)
                }
                PREDATOR -> {
                    context.getString(R.string.predator)
                }
                COUNT -> {
                    context.getString(R.string.herd)
                }
                ENVIRONMENT -> {
                    context.getString(R.string.environment)
                }
            }
        }
    }

}