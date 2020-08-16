package com.example.osmdroidexample.ui.trip

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.osmdroidexample.R
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

    private val pinnedLocations = mutableListOf<GeoPoint>()
    private val gpsTrail = Polyline() // GPS trail of current trip

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.trip_fragment, container, false)

        val viewModelFactory = TripViewModelFactory(requireNotNull(this.activity).application)

        viewModel = ViewModelProvider(
            this, viewModelFactory)[TripViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel

        val arguments = TripFragmentArgs.fromBundle(requireArguments())

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

                Log.d("#######", "TileSource: ${tileSource.minimumZoomLevel}---${tileSource.maximumZoomLevel}")

                mapView.setTileSource(tileSource)
                mapView.controller.zoomTo(tileSource.maximumZoomLevel.toDouble())
            } catch (e: Exception) {
                mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            }

        }

        Log.d("#######", "TileProvider: ${mapView.tileProvider.minimumZoomLevel}---${mapView.tileProvider.maximumZoomLevel}")

        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)

        MapAreaManager.getLastKnownLocation(requireContext())?.let {
            mapView.controller.animateTo(it)
        }

        pinCurrentLocationButton.setOnClickListener {
            pinCurrentLocation()
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