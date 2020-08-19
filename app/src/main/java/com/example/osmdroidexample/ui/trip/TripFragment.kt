package com.example.osmdroidexample.ui.trip

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.osmdroidexample.R
import com.example.osmdroidexample.database.AppDatabase
import com.example.osmdroidexample.databinding.TripFragmentBinding
import com.example.osmdroidexample.map.MapAreaManager
import kotlinx.android.synthetic.main.trip_fragment.*
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.lang.Exception

class TripFragment : Fragment() {

    private lateinit var viewModel: TripViewModel
    private lateinit var binding: TripFragmentBinding
    private lateinit var arguments: TripFragmentArgs

    private val pinnedLocations = mutableListOf<GeoPoint>()
    private val gpsTrail = Polyline() // GPS trail of current trip

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.trip_fragment, container, false)

        arguments = TripFragmentArgs.fromBundle(requireArguments())

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = TripViewModelFactory(
            arguments.mapAreaString,
            requireNotNull(this.activity).application,
            appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory)[TripViewModel::class.java]

        viewModel.mapArea.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.d("#######", "MapArea: " + it.toString())
                tripMapView.minZoomLevel = it.mapAreaMinZoom
                tripMapView.maxZoomLevel = it.mapAreaMaxZoom
                tripMapView.controller.zoomTo(it.mapAreaMinZoom)
                tripMapView.controller.animateTo(it.boundingBox.centerWithDateLine)
            }
        })

        binding.lifecycleOwner = viewLifecycleOwner
        binding.tripViewModel = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel

        val mapAreaString = "map_area_${arguments.mapAreaString}.sqlite"

        mapAreaNameTextView.text = mapAreaString

        setupMapView(tripMapView, mapAreaString)
    }

    private fun setupMapView(mapView: MapView, mapAreaName: String) {
        mapView.setUseDataConnection(false)
        mapView.isTilesScaledToDpi = true
        mapView.setMultiTouchControls(true)

        val mapAreaFile = context?.getDatabasePath(mapAreaName)

        if (mapAreaFile!!.exists()) {
            val offlineTileProvider = OfflineTileProvider(SimpleRegisterReceiver(context), arrayOf(mapAreaFile))
            mapView.tileProvider = offlineTileProvider

            try {
                //TODO: Offload to parallel async task
                val tileSourceString = offlineTileProvider.archives[0].tileSources.iterator().next()
                val tileSource = FileBasedTileSource.getSource(tileSourceString)

                mapView.setTileSource(tileSource)
                mapView.controller.zoomTo(tileSource.maximumZoomLevel.toDouble())
            } catch (e: Exception) {
                mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            }

        }


        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)

        MapAreaManager.getLastKnownLocation(requireContext())?.let {
            mapView.controller.animateTo(it)
        }

        pinCurrentLocationButton.setOnClickListener {
            pinCurrentLocation()
        }

        Log.d("########", "######")
        viewModel.mapArea.value?.let {
            Log.d("########", it.boundingBox.centerWithDateLine.toString())
            tripMapView.controller.animateTo(it.boundingBox.centerWithDateLine)
        }

        tripMapView.overlayManager.add(gpsTrail)

    }

    private fun pinCurrentLocation () {
        MapAreaManager.getLastKnownLocation(requireContext())?.let {
            pinnedLocations.add(it)

            val pinnedLocationMarker = Marker(tripMapView)
            pinnedLocationMarker.position = it
            pinnedLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            tripMapView.overlays.add(pinnedLocationMarker)
        }

        if (pinnedLocations.size > 0) {
            gpsTrail.setPoints(pinnedLocations)
        }

        tripMapView.invalidate()


        Log.d("#####", "Pinned Locations: ${pinnedLocations.toString()}")
        Log.d("#####", "Overlays: ${tripMapView.overlays.toString()}")
    }

}