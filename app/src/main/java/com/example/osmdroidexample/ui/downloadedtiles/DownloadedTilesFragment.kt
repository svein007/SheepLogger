package com.example.osmdroidexample.ui.downloadedtiles

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.osmdroidexample.R
import com.example.osmdroidexample.database.AppDatabase
import com.example.osmdroidexample.databinding.DownloadedTilesFragmentBinding
import com.example.osmdroidexample.map.MapAreaManager
import kotlinx.android.synthetic.main.downloaded_tiles_fragment.*

class DownloadedTilesFragment : Fragment() {

    private lateinit var viewModel: DownloadedTilesViewModel
    private lateinit var binding: DownloadedTilesFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.downloaded_tiles_fragment, container, false
        )

        val application = requireNotNull(this.activity).application
        val appDao = AppDatabase.getInstance(application).appDatabaseDao

        val viewModelFactory = DownloadedTilesViewModelFactory(application, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[DownloadedTilesViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.downliadedTilesViewModel = viewModel

        val adapter = MapAreaAdapter(MapAreaListItemListener {
            Log.d("#####", "Clicked MapArea ID: $it")
            Toast.makeText(requireContext(), "Clicked MapArea ID: $it", Toast.LENGTH_SHORT).show()

            // TODO: Navigate to Trip-Fragment
            findNavController().navigate(
                DownloadedTilesFragmentDirections.actionDownloadedTilesFragmentToTripFragment(it)
            )
        })

        binding.mapAreasRecyclerView.adapter = adapter

        viewModel.mapAreas.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        binding.mapAreasRecyclerView.addItemDecoration(DividerItemDecoration(application, DividerItemDecoration.VERTICAL))

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mapArchiveFilesPath = MapAreaManager.getStoredMapAreas(requireContext())

        mapAreaFilesTextView.text = mapArchiveFilesPath.joinToString(separator = ", \n")

        navigateToTripButton.setOnClickListener { v ->
            val mapAreaString = mapAreaNameEditText.text.toString()
            if ("map_area_${mapAreaString}.sqlite" in mapArchiveFilesPath) { //TODO: FIX
                /*
                findNavController().navigate(
                    DownloadedTilesFragmentDirections.actionDownloadedTilesFragmentToTripFragment(mapAreaString)
                )
                 */
            } else {
                Toast.makeText(context, "No such MapArea", Toast.LENGTH_LONG).show()
            }
        }
    }

}