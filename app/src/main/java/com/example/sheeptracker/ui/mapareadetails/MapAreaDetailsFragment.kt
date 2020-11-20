package com.example.sheeptracker.ui.mapareadetails

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
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

    /** Lifecycle **/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.map_area_details_fragment, container, false)

        setHasOptionsMenu(true)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.map_area_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_delete_map_area) {
            showDeleteMapAreaDialog()
            return true
        }
        return false
    }

    /** Helpers **/

    private fun showDeleteMapAreaDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_map_area))
            .setMessage(getString(R.string.delete_map_area_query))
            .setPositiveButton(getString(R.string.delete)) { dialog, which ->
                viewModel.deleteMapArea()
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            }
            .show()
    }

}