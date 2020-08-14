package com.example.osmdroidexample.map

import android.content.Context
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

class MapAreaManager {

    companion object {
        fun getStoredMapAreas(context: Context): List<String> {
            return  context.databaseList()
                .filter { filename -> filename.startsWith("map_area") }
                .filter { filename -> filename.endsWith(".sqlite") }
                .toList()
        }

        fun getOnlineTileSource(): OnlineTileSourceBase {
            return object : OnlineTileSourceBase("Kartverket - Norge Topografisk",
                0, 20, 256, "",
                arrayOf("https://opencache.statkart.no/gatekeeper/gk/gk.open_gmaps")) {

                override fun getTileURLString(pMapTileIndex: Long): String {
                    return (baseUrl + "?"
                            + "layers=topo4&"
                            + "zoom=" + MapTileIndex.getZoom(pMapTileIndex) + "&"
                            + "y=" + MapTileIndex.getY(pMapTileIndex) + "&"
                            + "x=" + MapTileIndex.getX(pMapTileIndex))
                }
            }
        }

        /* WORKING, but pixelated when zoomed far in...

        val wmsTileSource = KartverketWMSTileSource("Kartverket - Norge Topografisk",
            2, 20, 256, "png",
            arrayOf("https://openwms.statkart.no/skwms1/wms.topo4"), "topo4_WMS",
            "1.0.0",
            "EPSG:25833"
        )
         */

        //Log.d("########", wmsTileSource.getTileURLString(1))

        //val tileWriter = TileWriter()
        //val fileSystemProvider = MapTileFilesystemProvider(registerReciever, tileSource)

        // GEMFFileArchive.getGEMFFileArchive(mGemfArchiveFilename)
        /*
        val wmstilesource = WMSTileSource("WMS Kartverket Norge Grunnkart",
                arrayOf("https://opencache.statkart.no/gatekeeper/gk/gk.open_gmaps?layers=norges_grunnkart&zoom={z}&x={x}&y={y}"),
                "landareal","1.0.0","GetMap","default",4)
        */

    }

}