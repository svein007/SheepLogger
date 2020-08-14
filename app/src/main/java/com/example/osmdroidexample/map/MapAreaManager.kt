package com.example.osmdroidexample.map

import android.content.Context

class MapAreaManager {

    companion object {
        fun getStoredMapAreas(context: Context): List<String> {
            return  context.databaseList()
                .filter { filename -> filename.startsWith("map_area") }
                .filter { filename -> filename.endsWith(".sqlite") }
                .toList()
        }
    }

}