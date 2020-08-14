package com.example.osmdroidexample.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.osmdroidexample.R
import kotlinx.android.synthetic.main.main_fragment.*

import org.osmdroid.config.Configuration
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter
import org.osmdroid.tileprovider.tilesource.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private var cacheManager: CacheManager? = null

    private var locationManager: LocationManager? = null
    private var locationOverlay: MyLocationNewOverlay? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Configuration.getInstance().load(context?.applicationContext, PreferenceManager.getDefaultSharedPreferences(context?.applicationContext))

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID  // Required to do API calls to OSM servers

        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel

        tilesListButton.setOnClickListener { v ->
            this.findNavController().navigate(
                R.id.action_mainFragment_to_downloadedTilesFragment
            )
        }

        locationManager = this.context?.applicationContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        locationOverlay?.enableMyLocation()

        val tileSource = object : OnlineTileSourceBase("Kartverket - Norge Topografisk", 0,
                20, 256, "",
                //arrayOf("https://wms.geonorge.no/skwms1/wms.bakgrunnskart_havarealverktoey?service=WMS")) {
                //arrayOf("https://wms.geonorge.no/skwms1/wms.dybderelieffpolar_v02")) {

                // VIRKER... men må nok bruke WSM-url (cache er raskere men ikke like bra resolution)
                arrayOf("https://opencache.statkart.no/gatekeeper/gk/gk.open_gmaps")) {

                // https://openwms.statkart.no/skwms1/wms.norges_grunnkart?request=GetCapabilities&service=WMS
                // https://openwms.statkart.no/skwms1/wms.topo4?service=wms&Version=1.0.0&request=GetMap&layers=topo4_WMS&SRS=EPSG:25833&bbox=-127998,6377920,1145510,7976800&format=image/png&width=256&height=256

            override fun getTileURLString(pMapTileIndex: Long): String {
                return (baseUrl + "?"
                        + "layers=topo4&"
                        + "zoom=" + MapTileIndex.getZoom(pMapTileIndex) + "&"
                        + "y=" + MapTileIndex.getY(pMapTileIndex) + "&"
                        + "x=" + MapTileIndex.getX(pMapTileIndex))
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
        //mapView.setTileSource(TileSourceFactory.MAPNIK)

        val position = GeoPoint(65.0, 14.0)

        mapView.setTileSource(tileSource)

        mapView.setMultiTouchControls(true)

        mapView.minZoomLevel = 5.0
        mapView.maxZoomLevel = 20.0

        mapView.isTilesScaledToDpi = true
        mapView.isFlingEnabled = true

        mapView.setScrollableAreaLimitLatitude(72.0, 55.0, 0)
        mapView.setScrollableAreaLimitLongitude(-2.0, 33.0, 0)

        mapView.controller.setZoom(5.0)
        mapView.controller.setCenter(position)

        textView.text = "Zoom = " + mapView.zoomLevelDouble

        mapView.addMapListener(DelayedMapListener(object : MapListener {
            override fun onZoom(event: ZoomEvent?): Boolean {
                textView.text = "Zoom = " + mapView.zoomLevelDouble
                return false
            }

            override fun onScroll(event: ScrollEvent?): Boolean {
                return false
            }
        }))

        val marker = Marker(mapView)
        marker.position = position
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        mapView.overlays.add(marker)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            val currentLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            Log.d("#########", currentLocation.toString())
            mapView.controller.animateTo( GeoPoint(currentLocation))
        }

        mapView.overlays.add(locationOverlay)

        cacheManager = CacheManager(mapView)

        saveTileButton.setOnClickListener { v ->
            //Toast.makeText(context, "Save-button Clicked", Toast.LENGTH_LONG).show()

            val possibleNumOfTiles = cacheManager?.possibleTilesInArea(mapView.boundingBox, mapView.zoomLevelDouble.toInt(), 20) ?: 0
            Toast.makeText(context, "#Tiles="+possibleNumOfTiles, Toast.LENGTH_LONG).show()

            //TODO: Warn & confirm if users tries to download a large number of tiles! (show space usage?)
            if (possibleNumOfTiles > 10000) {
                Toast.makeText(context, ">10000 tiles, use smaller area", Toast.LENGTH_LONG).show()
            } else {
                //TODO: Download map tiles to storage
                val mapAreaFileName = mapNameEditText.text.toString()

                //val storageLocation = context?.getExternalFilesDir("database")
                //val storageLocation = context?.filesDir?.absolutePath

                //val mapArchivePath = "${storageLocation}/map_areas/${mapAreaFileName}.sqlite"
                val mapArchiveFile = context?.getDatabasePath("map_area_$mapAreaFileName")
                val mapArchivePath = mapArchiveFile.toString()

                Log.d("#######", "Store map-area at: " + mapArchivePath)

                val writer = SqliteArchiveTileWriter(mapArchivePath)
                val cacheManagerSqlliteArchive = CacheManager(mapView, writer)

                cacheManagerSqlliteArchive.downloadAreaAsync(context, mapView.boundingBox, mapView.zoomLevelDouble.toInt(), 20, object : CacheManager.CacheManagerCallback {
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
                        Toast.makeText(context, "Downloaded!", Toast.LENGTH_LONG).show()
                    }

                    override fun onTaskFailed(errors: Int) {
                    }

                })

            }

        }


    }

    override fun onResume() {
        super.onResume()

        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        mapView.onPause()
    }

}