package com.example.sheeptracker.ui.mapareadetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.MapAreaDetailsFragmentBinding

class MapAreaDetailsFragment : Fragment() {

    private lateinit var binding: MapAreaDetailsFragmentBinding

    private val args: MapAreaDetailsFragmentArgs by navArgs()

    private val viewModel: MapAreaDetailsViewModel by viewModels {
        MapAreaDetailsViewModelFactory(
            args.mapAreaId,
            requireActivity().application,
            AppDatabase.getInstance(requireContext()).appDatabaseDao
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.map_area_details_fragment, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.mapAreaDetailsFAB.setOnClickListener {
            findNavController().navigate(
                MapAreaDetailsFragmentDirections.actionMapAreaDetailsFragmentToMapAreaFragment(args.mapAreaId)
            )
        }

        viewModel.mapArea.observe(viewLifecycleOwner) {
            it?.let { mapArea ->
                val mapAreaString = mapArea.getSqliteFilename()
                binding.mapAreaDetailsMapView.setupStaticOfflineView(mapAreaString)
                binding.mapAreaDetailsMapView.maxZoomLevel = it.mapAreaMaxZoom - 1
                binding.mapAreaDetailsMapView.zoomAndCenterToDefault(mapArea)
            }
        }

        return binding.root
    }

}