package com.example.osmdroidexample.ui.trip

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
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

    private var gpsTrackingInProgress: Boolean = false
    private lateinit var viewModel: TripViewModel
    private lateinit var binding: TripFragmentBinding
    private lateinit var arguments: TripFragmentArgs

    private val pinnedLocations = mutableListOf<GeoPoint>()
    private val gpsTrail = Polyline() // GPS trail of current trip

    private lateinit var locationManager: LocationManager

    private val permissionRequestCode = 13

    private val gpsListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            Log.d("#####", "Location: " + location.toString())
            location?.let {
                pinLocation(GeoPoint(it))
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }

    }

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
            arguments.tripId,
            arguments.mapAreaId,
            requireNotNull(this.activity).application,
            appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory)[TripViewModel::class.java]

        viewModel.mapArea.observe(viewLifecycleOwner, {
            it?.let {
                Log.d("#######", "MapArea: " + it.toString())
                binding.tripMapView.minZoomLevel = it.mapAreaMinZoom
                binding.tripMapView.maxZoomLevel = it.mapAreaMaxZoom
                binding.tripMapView.controller.zoomTo(it.mapAreaMinZoom)
                binding.tripMapView.controller.animateTo(it.boundingBox.centerWithDateLine)

                val mapAreaString = it.getSqliteFilename()
                mapAreaNameTextView.text = mapAreaString
                setupMapView(binding.tripMapView, mapAreaString)
            }
        })

        binding.lifecycleOwner = viewLifecycleOwner
        binding.tripViewModel = viewModel

        return binding.root
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
            } catch (e: Exception) {
                mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            }
        } else {
            Toast.makeText(requireContext(), "Could not find MapArea", Toast.LENGTH_LONG).show()
        }

        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)

        binding.pinCurrentLocationButton.setOnClickListener {
            pinCurrentLocation()
        }

        Log.d("########", "######")
        viewModel.mapArea.value?.let {
            Log.d("########", it.boundingBox.centerWithDateLine.toString())
            binding.tripMapView.controller.animateTo(it.boundingBox.centerWithDateLine)
        }

        binding.tripMapView.overlayManager.add(gpsTrail)

        locationManager = requireContext().applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        binding.startGpsLogButton.setOnClickListener {

            if (!gpsTrackingInProgress) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 20.0f, gpsListener)
                }
            }

            gpsTrackingInProgress = true
        }

        binding.stopGpsLogButton.setOnClickListener {

            if (gpsTrackingInProgress) {
                locationManager.removeUpdates(gpsListener)
            }

            gpsTrackingInProgress = false
        }

    }

    override fun onResume() {
        super.onResume()
        if (gpsTrackingInProgress) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 20.0f, gpsListener)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (gpsTrackingInProgress) {
            locationManager.removeUpdates(gpsListener)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MapAreaManager.getLastKnownLocation(requireContext(), requireActivity(), permissionRequestCode, false)?.let {
                    pinCurrentLocation()
                }
            }
        }
    }

    private fun pinCurrentLocation () {
        MapAreaManager.getLastKnownLocation(requireContext(), requireActivity(), permissionRequestCode)?.let {
            pinLocation(it)
        }
    }

    private fun pinLocation (geoPoint: GeoPoint) {
        pinnedLocations.add(geoPoint)

        val pinnedLocationMarker = Marker(tripMapView)
        pinnedLocationMarker.position = geoPoint
        pinnedLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        binding.tripMapView.overlays.add(pinnedLocationMarker)

        if (pinnedLocations.size > 0) {
            gpsTrail.setPoints(pinnedLocations)
        }

        binding.tripMapView.invalidate()

        Log.d("#####", "Pinned Locations: ${pinnedLocations.toString()}")
        Log.d("#####", "Overlays: ${tripMapView.overlays.toString()}")
    }

}