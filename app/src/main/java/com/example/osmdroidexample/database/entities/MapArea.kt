package com.example.osmdroidexample.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.osmdroid.util.BoundingBox

@Entity(tableName = "map_area_table")
data class MapArea (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "map_area_id")
    var mapAreaId: Long = 0L,

    @ColumnInfo(name = "map_area_name")
    var mapAreaName: String = "",

    @ColumnInfo(name = "map_area_min_zoom")
    var mapAreaMinZoom: Double = 0.0,

    @ColumnInfo(name = "map_area_max_zoom")
    var mapAreaMaxZoom: Double = 0.0,

    @ColumnInfo(name = "map_area_bounding_box")
    var boundingBox: BoundingBox = BoundingBox()

){

    fun getSqliteFilename(): String {
        val cleanedMapAreaName = mapAreaName.replace(Regex.fromLiteral("[^\\w.-]"), "_")
        return "map_area_$cleanedMapAreaName.sqlite"
    }

}