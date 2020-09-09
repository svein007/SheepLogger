package com.example.sheeptracker.database

import androidx.room.TypeConverter
import com.example.sheeptracker.database.entities.Counter
import org.osmdroid.util.BoundingBox
import java.util.*

class Converters {

    @TypeConverter
    fun stringToBoundingBox(string: String): BoundingBox? {

        val bboxValues = string.split(";").map { x -> x.trim().toDouble() }

        return BoundingBox(bboxValues[0], bboxValues[1], bboxValues[2], bboxValues[3])
    }

    @TypeConverter
    fun boundingBoxToString(boundingBox: BoundingBox): String {

        val bboxStr = boundingBox
            .toString()
            .replace(":", "")
            .replace("N", "")
            .replace("E", "")
            .replace("S", "")
            .replace("W", "")
            .trim()

        return bboxStr

    }

    @TypeConverter
    fun toCountType(value: Int) = enumValues<Counter.CountType>()[value]

    @TypeConverter
    fun fromCountType(value: Counter.CountType) = value.ordinal

    @TypeConverter
    fun toDate(value: Long?): Date? {
        return if (value != null) {
            Date(value)
        } else {
            null
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

}