package com.example.sheeptracker.ui.herdobservationdetails

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.HerdObservationDetailsFragmentBinding
import com.example.sheeptracker.map.MapAreaManager
import com.google.android.material.snackbar.Snackbar
import org.osmdroid.util.GeoPoint

class HerdObservationDetailsFragment : Fragment() {

    private lateinit var binding: HerdObservationDetailsFragmentBinding
    private val arguments: HerdObservationDetailsFragmentArgs by navArgs()
    private val viewModel: HerdObservationDetailsViewModel by viewModels {
        HerdObservationDetailsViewModelFactory(
            arguments.observationId,
            arguments.tripId,
            arguments.obsLat.toDoubleOrNull() ?: 0.0,
            arguments.obsLon.toDoubleOrNull() ?: 0.0,
            requireActivity().application,
            AppDatabase.getInstance(requireContext()).appDatabaseDao
        )
    }

    private var shouldDeleteObservation = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.herd_observation_details_fragment, container, false
        )

        setHasOptionsMenu(true)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val counterAdapter = CounterAdapter(
            CounterListItemListener {
                it.inc()
                binding.counterRV.adapter?.notifyDataSetChanged()
                viewModel.onUpdateCounter(it)
            },
            CounterListItemListener {
                it.dec()
                binding.counterRV.adapter?.notifyDataSetChanged()
                viewModel.onUpdateCounter(it)
            },
            CounterListItemListener {
                binding.counterRV.adapter?.notifyDataSetChanged()
                viewModel.onUpdateCounter(it)
            }
        )

        binding.counterRV.adapter = counterAdapter

        viewModel.counters.observe(viewLifecycleOwner, {
            it?.let {
                counterAdapter.submitList(it)
            }
        })

        viewModel.trip.observe(viewLifecycleOwner) {
            it //HACK: to populate the livedata
        }

        viewModel.observation.observe(viewLifecycleOwner) {
            drawObservation()
            zoomToGeoPoints()
        }

        viewModel.tripMapPoints.observe(viewLifecycleOwner) {
            it?.let {
                val geoPoints = it.map { tripMapPoint -> GeoPoint(tripMapPoint.tripMapPointLat, tripMapPoint.tripMapPointLon) }
                binding.herdObservationMapView.drawSimpleGPSTrail(geoPoints, true)
                drawObservation()
                zoomToGeoPoints()
            }
        }

        viewModel.mapArea.observe(viewLifecycleOwner) {
            it?.let { mapArea ->
                val mapAreaString = mapArea.getSqliteFilename()
                binding.herdObservationMapView.setupStaticOfflineView(mapAreaString)
                binding.herdObservationMapView.maxZoomLevel = it.mapAreaMaxZoom - 1
                binding.herdObservationMapView.zoomOutAndCenter(mapArea)
            }
        }

        shouldDeleteObservation = arguments.observationId < 0

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.herd_details_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.forEachIndexed { index, item ->
            when (item.itemId) {
                R.id.mi_delete_secondary_trip_map_point -> {
                    item.isVisible = viewModel.observation.value?.observationSecondaryTripMapPointId != null
                }
                R.id.mi_set_secondary_trip_map_point -> {
                    viewModel.trip.value?.tripFinished?.let {
                        item.isVisible = !it
                    }
                }
                R.id.mi_add_herd_obs -> {
                    item.title = if (arguments.observationId < 0) getString(R.string.add) else getString(R.string.save)
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_add_herd_obs -> {
                shouldDeleteObservation = false
                viewModel.onUpdateObservation()
                findNavController().navigateUp()
                return true
            }
            R.id.mi_swiper -> {
                findNavController().navigate(
                    HerdObservationDetailsFragmentDirections.actionAnimalCountersDetailsFragmentToSwiperFragment(
                        viewModel.obsId
                    )
                )
                return true
            }
            R.id.mi_delete_herd_obs -> {
                showDeleteObservationDialog()
                return true
            }
            R.id.mi_set_secondary_trip_map_point -> {
                addSecondaryTripMapPoint()
                return true
            }
            R.id.mi_delete_secondary_trip_map_point -> {
                viewModel.onDeleteSecondaryTripMapPoint()
                Snackbar
                    .make(binding.root, getString(R.string.secondary_trip_map_point_deleted), Snackbar.LENGTH_LONG)
                    .show()
                return true
            }
        }

        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (shouldDeleteObservation) {
            viewModel.onDeleteObservation()
        }
        if (!requireActivity().isChangingConfigurations) {
            requireActivity().viewModelStore.clear() // DANGEROUS??
        }
    }

    /** Helpers **/

    private fun showDeleteObservationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_observation))
            .setMessage(getString(R.string.delete_observation_query))
            .setPositiveButton(getString(R.string.delete)) { dialog, which ->
                viewModel.onDeleteObservation()
                findNavController().navigateUp()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            }
            .show()
    }

    private fun addSecondaryTripMapPoint() {
        val currentPosition = MapAreaManager.getLastKnownLocation(requireContext(), requireActivity(), 0, false)
        if (currentPosition != null) {
            viewModel.onAddSecondaryTripMapPoint(currentPosition)
        }
        Snackbar
            .make(binding.root, getString(R.string.secondary_trip_map_point_added), Snackbar.LENGTH_LONG)
            .show()
    }

    private fun drawObservation() {
        if (viewModel.observation.value != null && viewModel.tripMapPoints.value != null) {
            binding.herdObservationMapView.drawSimpleObservationLinesAndMarkers(
                arrayListOf(viewModel.observation.value!!),
                viewModel.tripMapPoints.value!!
            )
        }
    }

    private fun zoomToGeoPoints() {
        val geoPoints = ArrayList<GeoPoint>()
        viewModel.observation.value?.let { obs ->
            viewModel.tripMapPoints.value?.let { points ->
                geoPoints.addAll(
                    points
                        .filter { p -> p.tripMapPointId == obs.observationOwnerTripMapPointId || p.tripMapPointId == obs.observationSecondaryTripMapPointId }
                        .map { p -> GeoPoint(p.tripMapPointLat, p.tripMapPointLon) }
                )
            }
            geoPoints.add(GeoPoint(obs.observationLat, obs.observationLon))
        }
        binding.herdObservationMapView.zoomToGeoPoints(geoPoints)
    }

}