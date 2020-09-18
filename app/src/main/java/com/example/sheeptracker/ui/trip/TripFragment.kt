package com.example.sheeptracker.ui.trip

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.databinding.TripFragmentBinding
import com.example.sheeptracker.service.LocationService
import org.osmdroid.events.*
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.lang.Exception
import kotlin.collections.ArrayList

class TripFragment : Fragment() {

    private lateinit var viewModel: TripViewModel
    private lateinit var binding: TripFragmentBinding
    private lateinit var arguments: TripFragmentArgs

    private val gpsTrail = Polyline() // GPS trail of current trip
    private val gpsMarkers = ArrayList<Marker>() // To be able to delete old markers
    private val observationMarkers = ArrayList<Marker>() // To be able to delete old markers
    private val observationPolylines = ArrayList<Polyline>()

    private val eventsOverlay = MapEventsOverlay( object : MapEventsReceiver{
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            return false
        }

        override fun longPressHelper(geoPoint: GeoPoint?): Boolean {
            showNewObservationDialog(geoPoint)

            return true
        }

    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.trip_fragment, container, false)

        setHasOptionsMenu(true)

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
                Log.d("#####", "MapArea sql: mapAreaString")
                setupMapView(binding.tripMapView, mapAreaString)
            }
        })

        viewModel.tripMapPoints.observe(viewLifecycleOwner, {
            it?.let {
                if (it.isNotEmpty()) {
                    drawGpsTrail()
                    drawObservationLines()
                    binding.tripMapView.invalidate()
                }
            }
        })

        viewModel.observations.observe(viewLifecycleOwner, {
            it?.let {
                drawObservationLines()
                binding.tripMapView.invalidate()
            }
        })

        binding.observationsButton.setOnClickListener {
            findNavController().navigate(
                TripFragmentDirections.actionTripFragmentToObservationsFragment(arguments.tripId)
            )
        }

        viewModel.latestLocation.observe(viewLifecycleOwner) {
            it?.let {
                if (viewModel.isFollowingGPS.value!!) {
                    binding.tripMapView.controller.animateTo(GeoPoint(it.latitude, it.longitude))
                }
            }
        }

        viewModel.isFollowingGPS.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    binding.gpsFollowButton.setImageResource(R.drawable.ic_baseline_gps_fixed_24)
                } else {
                    binding.gpsFollowButton.setImageResource(R.drawable.ic_baseline_gps_not_fixed_24)
                }
            }
        }

        binding.lifecycleOwner = viewLifecycleOwner
        binding.tripViewModel = viewModel

        startLocationService()

        return binding.root
    }

    private fun setupMapView(mapView: MapView, mapAreaName: String) {
        mapView.setUseDataConnection(false)
        mapView.isTilesScaledToDpi = true
        mapView.setMultiTouchControls(true)
        mapView.isLongClickable = true

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
        binding.tripMapView.overlays.add(locationOverlay)

        mapView.overlayManager.add(eventsOverlay)

        viewModel.mapArea.value?.let {
            Log.d("########", it.boundingBox.centerWithDateLine.toString())
            binding.tripMapView.controller.animateTo(it.boundingBox.centerWithDateLine)
        }

        binding.startGpsLogButton.setOnClickListener {
            startLocationService()
        }

        binding.stopGpsLogButton.setOnClickListener {
            stopLocationService()
        }

        mapView.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                viewModel.stopLocationUpdates()
                viewModel.isFollowingGPS.value = false
            }
            false
        }

        viewModel.triggerLocationLiveDataUpdate()

        mapView.invalidate()
    }

    private fun drawObservationLines() {
        if (viewModel.tripMapPoints.value == null || viewModel.observations.value == null) {
            return
        }

        val observationsGeoPoints = viewModel.observations.value!!.map { observation ->
            GeoPoint(observation.observationLat, observation.observationLon)
        }

        val observationTripMapGeoPoints = viewModel.observations.value!!.map { observation ->
            val tripMapPoint = viewModel.tripMapPoints.value?.first { tripMapPoint ->
                tripMapPoint.tripMapPointId == observation.observationOwnerTripMapPointId
            }
            GeoPoint(tripMapPoint!!.tripMapPointLat, tripMapPoint!!.tripMapPointLon)
        }

        binding.tripMapView.overlayManager.removeAll(observationMarkers)
        observationMarkers.clear()

        observationsGeoPoints.forEachIndexed { i, geoPoint ->
            val marker = Marker(binding.tripMapView)
            marker.position = geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            if (viewModel.observations.value!![i].observationType == Observation.ObservationType.DEAD) {
                marker.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_report_problem_24, null)
            } else {
                marker.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_remove_red_eye_24, null)
            }
            observationMarkers.add(marker)
        }


        observationPolylines.clear()

        for (i in observationTripMapGeoPoints.indices) {
            val line = Polyline()
            line.setPoints(listOf(observationTripMapGeoPoints[i], observationsGeoPoints[i]))
            line.outlinePaint.color = Color.BLUE
            observationPolylines.add(line)
        }

        binding.tripMapView.overlayManager.addAll(observationPolylines)
        binding.tripMapView.overlayManager.addAll(observationMarkers)
    }

    private fun drawGpsTrail() {
        if (viewModel.tripMapPoints.value == null) {
            return
        }

        val tripMapPoints = viewModel.tripMapPoints.value!!.map { tripMapPoint ->
            GeoPoint(tripMapPoint.tripMapPointLat, tripMapPoint.tripMapPointLon)
        }

        gpsTrail.setPoints(tripMapPoints)

        binding.tripMapView.overlayManager.removeAll(gpsMarkers)
        gpsMarkers.clear()

        tripMapPoints.forEach { geoPoint ->
            val marker = Marker(binding.tripMapView)
            marker.position = geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            gpsMarkers.add(marker)
        }

        gpsMarkers.first().let {marker ->
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            marker.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_trip_origin_24, null)
            marker.title = "Start"
        }

        // binding.tripMapView.overlayManager.addAll(gpsMarkers)
        binding.tripMapView.overlayManager.add(gpsMarkers.first())
    }

    override fun onResume() {
        super.onResume()

        binding.tripMapView.onResume()

        binding.tripMapView.overlayManager.add(gpsTrail)
    }

    override fun onPause() {
        super.onPause()

        binding.tripMapView.overlayManager.remove(gpsTrail)

        binding.tripMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationService.stopService(requireContext())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.trip_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_delete_trip) {
            viewModel.deleteTrip()
            findNavController().navigateUp()
            return true
        }
        return false
    }

    private fun startLocationService() {
        if (!viewModel.isTrackingGPS.value!!) {
            LocationService.startService(requireContext(), arguments.tripId)
            viewModel.isTrackingGPS.value = true
        }
    }

    private fun stopLocationService() {
        LocationService.stopService(requireContext())
        viewModel.isTrackingGPS.value = false
    }

    private fun showNewObservationDialog(geoPoint: GeoPoint?) {
        val observationTypeAlertDialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Select Observation Type")
                setItems(arrayOf("Animal Count", "Dead Animal")){ dialogInterface, index ->
                    if (index == 0) {
                        findNavController().navigate(
                            TripFragmentDirections.actionTripFragmentToAddObservationFragment(
                                arguments.tripId,
                                "${geoPoint?.latitude}",
                                "${geoPoint?.longitude}"
                            )
                        )
                    } else if (index == 1) {
                        findNavController().navigate(
                            TripFragmentDirections.actionTripFragmentToAddDeadAnimalFragment(
                                arguments.tripId,
                                "${geoPoint?.latitude}",
                                "${geoPoint?.longitude}"
                            )
                        )
                    }
                }
            }
            builder.create()
        }

        observationTypeAlertDialog?.show()
    }

}