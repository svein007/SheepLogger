package com.example.osmdroidexample.database

import androidx.room.TypeConverter
import org.osmdroid.util.BoundingBox

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
            .replace(":","")
            .replace("N","")
            .replace("E","")
            .replace("S","")
            .replace("W","")
            .trim()

        return bboxStr

    }

}