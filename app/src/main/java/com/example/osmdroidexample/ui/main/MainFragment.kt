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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.osmdroidexample.R
import com.example.osmdroidexample.database.AppDatabase
import com.example.osmdroidexample.database.entities.MapArea
import com.example.osmdroidexample.databinding.MainFragmentBinding
import com.example.osmdroidexample.map.MapAreaManager
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
import kotlin.math.floor
import kotlin.math.roundToInt

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding

    private var cacheManager: CacheManager? = null

    private var locationManager: LocationManager? = null
    private var locationOverlay: MyLocationNewOverlay? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.main_fragment, container, false
        )

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = MainViewModelFactory(application, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[MainViewModel::class.java]

        // Configuration.getInstance().load(context?.applicationContext, PreferenceManager.getDefaultSharedPreferences(context?.applicationContext))

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID  // Required to do API calls to OSM servers

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel

        tilesListButton.setOnClickListener { v ->
            this.findNavController().navigate(
                R.id.action_mainFragment_to_downloadedTilesFragment
            )
        }

        locationManager = this.context?.applicationContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        locationOverlay?.enableMyLocation()

        val tileSource = MapAreaManager.getOnlineTileSource()

        val position = GeoPoint(65.0, 14.0)

        mapView.setTileSource(tileSource)

        mapView.setMultiTouchControls(true)

        mapView.minZoomLevel = 5.0
        mapView.maxZoomLevel = 20.0

        mapView.isTilesScaledToDpi = true
        mapView.isFlingEnabled = true

        mapView.setScrollableAreaLimitLatitude(72.0, 55.0, 0)
        mapView.setScrollableAreaLimitLongitude(-2.0, 33.0, 0)

        mapView.controller.setZoom(15.0)
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

        MapAreaManager.getLastKnownLocation(requireContext())?.let {
            mapView.controller.animateTo(it)
        }

        mapView.overlays.add(locationOverlay)

        cacheManager = CacheManager(mapView)

        saveTileButton.setOnClickListener { v ->
            val possibleNumOfTiles = cacheManager?.possibleTilesInArea(mapView.boundingBox, mapView.zoomLevelDouble.toInt(), 20) ?: 0
            Toast.makeText(context, "#Tiles="+possibleNumOfTiles, Toast.LENGTH_LONG).show()

            //TODO: Warn & confirm if users tries to download a large number of tiles! (show space usage?)
            if (possibleNumOfTiles > 10000) {
                Toast.makeText(context, ">10000 tiles, use smaller area", Toast.LENGTH_LONG).show()
            } else {
                val mapAreaFileName = mapNameEditText.text.toString()

                MapAreaManager.storeMapArea(requireContext(), mapView, mapAreaFileName)

                // TODO: store MapArea in AppDB (refactor w/ MapAreaManager to one method?)
                val mapArea = MapArea(
                    mapAreaName = mapAreaFileName,
                    mapAreaMinZoom = floor(mapView.zoomLevelDouble),
                    mapAreaMaxZoom = mapView.maxZoomLevel,
                    boundingBox = mapView.boundingBox
                )

                viewModel.storeMapArea(mapArea)
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