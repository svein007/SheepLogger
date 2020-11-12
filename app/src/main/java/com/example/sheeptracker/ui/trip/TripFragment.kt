package com.example.sheeptracker.ui.trip

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.view.forEachIndexed
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.utils.*
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.databinding.TripFragmentBinding
import com.example.sheeptracker.map.MapAreaManager
import com.example.sheeptracker.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.events.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class TripFragment : Fragment() {

    private lateinit var viewModel: TripViewModel
    private lateinit var binding: TripFragmentBinding
    private lateinit var arguments: TripFragmentArgs
    private lateinit var appDao: AppDao

    private val eventsOverlay = MapEventsOverlay( object : MapEventsReceiver{
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            return false
        }

        override fun longPressHelper(geoPoint: GeoPoint?): Boolean {
            viewModel.isTripFinished.value?.let {
                if (!it) {
                    showNewObservationDialog(geoPoint)
                }
            }
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

        appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = TripViewModelFactory(
            arguments.tripId,
            requireNotNull(this.activity).application,
            appDao
        )

        viewModel = ViewModelProvider(
            this, viewModelFactory)[TripViewModel::class.java]

        binding.observationsFAB.setOnClickListener {
            findNavController().navigate(
                TripFragmentDirections.actionTripFragmentToObservationsFragment(arguments.tripId)
            )
        }

        viewModel.mapArea.observe(viewLifecycleOwner, {
            it?.let {
                Log.d("#######", "MapArea: " + it.toString())
                binding.tripMapView.minZoomLevel = it.mapAreaMinZoom
                binding.tripMapView.maxZoomLevel = it.mapAreaMaxZoom
                binding.tripMapView.controller.zoomTo(it.mapAreaMinZoom)
                binding.tripMapView.controller.animateTo(it.boundingBox.centerWithDateLine)
                binding.tripMapView.setScrollableAreaLimitLatitude(it.boundingBox.latNorth, it.boundingBox.latSouth, 500)
                binding.tripMapView.setScrollableAreaLimitLongitude(it.boundingBox.lonWest, it.boundingBox.lonEast, 500)

                val mapAreaString = it.getSqliteFilename()
                Log.d("#####", "MapArea sql: mapAreaString")
                setupMapView(binding.tripMapView, mapAreaString, it)
            }
        })

        viewModel.tripMapPoints.observe(viewLifecycleOwner, {
            it?.let {
                if (it.isNotEmpty()) {
                    binding.tripMapView.drawFullGPSTrail(it, viewModel.trip.value?.tripFinished ?: false)
                    drawObservations()
                    binding.tripMapView.invalidate()
                }
            }
        })

        viewModel.observations.observe(viewLifecycleOwner, {
            it?.let {
                drawObservations()
                binding.tripMapView.invalidate()
            }
        })

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

        viewModel.isTripFinished.observe(viewLifecycleOwner) {
            it?.let {
                if (!it) {
                    startLocationService()
                }
                viewModel.tripMapPoints.value?.let { points ->
                    if (points.isNotEmpty()) {
                        binding.tripMapView.drawFullGPSTrail(points, it)
                    }
                }
                viewModel.isFollowingGPS.value = !it
            }
        }

        binding.lifecycleOwner = viewLifecycleOwner
        binding.tripViewModel = viewModel

        binding.tripMotionLayout.progress = viewModel.motionLayoutProgress

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMapView(mapView: MapView, mapAreaName: String, mapArea: MapArea) {
        if (!binding.tripMapView.setupNormalOfflineView(mapAreaName, mapArea)) {
            Toast.makeText(requireContext(), "Could not find MapArea", Toast.LENGTH_LONG).show()
        }

        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        locationOverlay.enableMyLocation()
        binding.tripMapView.overlays.add(locationOverlay)

        mapView.overlayManager.add(eventsOverlay)

        binding.tripMapView.zoomAndCenterToDefault(mapArea)

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

    private fun drawObservations() {
        if (viewModel.observations.value != null && viewModel.tripMapPoints.value != null) {
            binding.tripMapView.drawFullObservationLinesAndMarkers(
                viewModel.observations.value!!,
                viewModel.tripMapPoints.value!!
            )

            CoroutineScope(Dispatchers.Main).launch {
                val shortObservationDescriptions = ArrayList<String>()
                viewModel.observations.value!!.forEachIndexed { i, observation ->
                    val observationShortDescription = getObservationShortDesc(appDao, binding.root.context, observation).replace("\n", "")
                    shortObservationDescriptions.add("\n${observationShortDescription}")
                }
                binding.tripMapView.attachObservationMarkerSnippets(shortObservationDescriptions)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.tripMapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        binding.tripMapView.onPause()

        viewModel.motionLayoutProgress = binding.tripMotionLayout.progress.roundToInt().toFloat()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationService.stopService(requireContext())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.trip_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.forEachIndexed { index, item ->
            when (item.itemId) {
                R.id.mi_start_tracking -> {
                    viewModel.isTripFinished.value?.let { tripFinished ->
                        viewModel.isTrackingGPS.value?.let {
                            item.setVisible(!tripFinished && !it)
                        }
                    }
                }
                R.id.mi_stop_tracking -> {
                    viewModel.isTripFinished.value?.let { tripFinished ->
                        viewModel.isTrackingGPS.value?.let {
                            item.setVisible(!tripFinished && it)
                        }
                    }
                }
                R.id.mi_finish_trip -> {
                    viewModel.isTripFinished.value?.let {
                        item.setVisible(!it)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_delete_trip -> {
                showDeleteTripDialog()
                return true
            }
            R.id.mi_start_tracking -> {
                startLocationService()
                return true
            }
            R.id.mi_stop_tracking -> {
                stopLocationService()
                return true
            }
            R.id.mi_finish_trip -> {
                showFinishTripDialog()
                return true
            }
        }
        return false
    }

    private fun showFinishTripDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.finish_trip))
            .setMessage(getString(R.string.finish_trip_query))
            .setPositiveButton(getString(R.string.finish)) { dialog, which ->
                viewModel.onFinishTrip()
                findNavController().navigateUp()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            }
            .show()
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
        val observationTypes = Observation.ObservationType.values().map { obsType -> obsType.getString(requireContext()) }

        val observationTypeAlertDialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(getString(R.string.select_observation_type))
                setItems(observationTypes.toTypedArray()){ dialogInterface, index ->

                    val location = MapAreaManager.getLastKnownLocation(
                        context,
                        null,
                        1
                    )

                    if (location == null) {
                        Toast.makeText(requireContext(), getString(R.string.unable_gps), Toast.LENGTH_LONG).show()
                        return@setItems
                    }

                    when (index) {
                        0 -> {
                            findNavController().navigate(
                                TripFragmentDirections.actionTripFragmentToAddObservationFragment(
                                    arguments.tripId,
                                    "${geoPoint?.latitude}",
                                    "${geoPoint?.longitude}"
                                )
                            )
                        }
                        1 -> {
                            findNavController().navigate(
                                TripFragmentDirections.actionTripFragmentToAddDeadAnimalFragment(
                                    arguments.tripId,
                                    "${geoPoint?.latitude}",
                                    "${geoPoint?.longitude}",
                                    Observation.ObservationType.DEAD.ordinal
                                )
                            )
                        }
                        2 -> {
                            findNavController().navigate(
                                TripFragmentDirections.actionTripFragmentToAddDeadAnimalFragment(
                                    arguments.tripId,
                                    "${geoPoint?.latitude}",
                                    "${geoPoint?.longitude}",
                                    Observation.ObservationType.INJURED.ordinal
                                )
                            )
                        }
                        3, 4 -> {
                            findNavController().navigate(
                                TripFragmentDirections.actionTripFragmentToPredatorRegistrationFragment(
                                    "${geoPoint?.latitude}",
                                    "${geoPoint?.longitude}",
                                ).setTripId(arguments.tripId).setObsType(index)
                            )
                        }
                    }
                }
            }
            builder.create()
        }

        observationTypeAlertDialog?.show()
    }

    private fun showDeleteTripDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_trip))
            .setMessage(getString(R.string.delete_trip_query))
            .setPositiveButton(getString(R.string.delete)) { dialog, which ->
                viewModel.deleteTrip()
                findNavController().popBackStack(R.id.startFragment, false)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            }
            .show()
    }

}