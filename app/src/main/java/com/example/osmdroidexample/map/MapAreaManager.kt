package com.example.osmdroidexample.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.osmdroidexample.ui.MainActivity
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView

class MapAreaManager {

    companion object {

        fun getLastKnownLocation(context: Context, activity: Activity, requestCode: Int, requestPermissionIfNotGranted: Boolean = true): GeoPoint? {
            val locationManager = context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return GeoPoint(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
            } else {
                if (requestPermissionIfNotGranted) {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), requestCode)
                }
            }
            return null
        }

        fun getStoredMapAreas(context: Context): List<String> {
            return  context.databaseList()
                .filter { filename -> filename.startsWith("map_area") }
                .filter { filename -> filename.endsWith(".sqlite") }
                .toList()
        }

        fun storeMapArea(context: Context, mapView: MapView, mapAreaName: String) {
            val mapAreaFilename = context.getDatabasePath("map_area_$mapAreaName.sqlite").toString()
            Log.d("#######", "Should store map-area at: $mapAreaFilename")

            val sqliteArchiveTileWriterCacheManager = CacheManager(mapView,
                SqliteArchiveTileWriter(mapAreaFilename))

            sqliteArchiveTileWriterCacheManager.downloadAreaAsync(context,
                mapView.boundingBox,
                mapView.zoomLevelDouble.toInt(),
                mapView.maxZoomLevel.toInt(),
                object : CacheManager.CacheManagerCallback {
                    override fun downloadStarted() {
                    }

                    override fun updateProgress(
                        progress: Int,
                        currentZoomLevel: Int,
                        zoomMin: Int,
                        zoomMax: Int
                    ) {
                    }

                    override fun setPossibleTilesInArea(total: Int) {
                    }

                    override fun onTaskComplete() {
                        Toast.makeText(context, "Downloaded: $mapAreaName", Toast.LENGTH_LONG).show()
                    }

                    override fun onTaskFailed(errors: Int) {
                    }
                }
            )

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