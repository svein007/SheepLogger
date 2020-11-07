package com.example.sheeptracker.ui.tripdetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.TripDetailsFragmentBinding
import org.osmdroid.util.GeoPoint

class TripDetailsFragment : Fragment() {

    lateinit var binding: TripDetailsFragmentBinding

    val viewModel: TripDetailsViewModel by viewModels {
        TripDetailsViewModelFactory(
            TripDetailsFragmentArgs.fromBundle(requireArguments()).tripId,
            requireNotNull(activity).application,
            AppDatabase.getInstance(requireNotNull(activity).application).appDatabaseDao
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.trip_details_fragment, container, false)

        binding.mapOverlayClickView.setOnClickListener {
            findNavController().navigate(
                TripDetailsFragmentDirections.actionTripDetailsFragmentToTripFragment(
                    TripDetailsFragmentArgs.fromBundle(requireArguments()).tripId
                )
            )
        }

        binding.mapFAB.setOnClickListener {
            findNavController().navigate(
                TripDetailsFragmentDirections.actionTripDetailsFragmentToTripFragment(
                    TripDetailsFragmentArgs.fromBundle(requireArguments()).tripId
                )
            )
        }

        viewModel.mapArea.observe(viewLifecycleOwner) {
            it?.let { mapArea ->
                val mapAreaString = mapArea.getSqliteFilename()
                binding.tripDetailsMapView.setupStaticOfflineView(mapAreaString)
                binding.tripDetailsMapView.maxZoomLevel = it.mapAreaMaxZoom
            }
        }

        viewModel.tripMapPoints.observe(viewLifecycleOwner) {
            it?.let {
                val geoPoints = it.map { tripMapPoint -> GeoPoint(tripMapPoint.tripMapPointLat, tripMapPoint.tripMapPointLon) }
                binding.tripDetailsMapView.drawSimpleGPSTrail(geoPoints, true)
                zoomToGeoPoints()
                drawObservations()
            }
        }

        viewModel.observations.observe(viewLifecycleOwner) {
            it?.let {
                zoomToGeoPoints()
                drawObservations()
            }
        }

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onPause() {
        super.onPause()

        binding.tripDetailsMapView.onPause()
    }

    override fun onResume() {
        super.onResume()

        binding.tripDetailsMapView.onResume()
    }

    /** Helpers **/

    private fun zoomToGeoPoints() {
        val geoPoints = ArrayList<GeoPoint>()
        viewModel.tripMapPoints.value?.let {
            geoPoints.addAll(it.map { p -> GeoPoint(p.tripMapPointLat, p.tripMapPointLon) })
        }
        viewModel.observations.value?.let {
            geoPoints.addAll(it.map { o -> GeoPoint(o.observationLat, o.observationLon) })
        }
        binding.tripDetailsMapView.zoomToGeoPoints(geoPoints)
    }

    private fun drawObservations() {
        if (viewModel.observations.value != null && viewModel.tripMapPoints.value != null) {
            binding.tripDetailsMapView.drawSimpleObservationLinesAndMarkers(
                viewModel.observations.value!!,
                viewModel.tripMapPoints.value!!
            )
        }
    }

}