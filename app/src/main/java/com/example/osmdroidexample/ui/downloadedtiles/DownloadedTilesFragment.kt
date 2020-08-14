package com.example.osmdroidexample.ui.downloadedtiles

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.osmdroidexample.R
import com.example.osmdroidexample.map.MapAreaManager
import kotlinx.android.synthetic.main.downloaded_tiles_fragment.*

class DownloadedTilesFragment : Fragment() {

    private lateinit var viewModel: DownloadedTilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.downloaded_tiles_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DownloadedTilesViewModel::class.java)
        // TODO: Use the ViewModel

        val mapArchiveFilesPath = MapAreaManager.getStoredMapAreas(requireContext())

        mapAreaFilesTextView.text = mapArchiveFilesPath.joinToString(separator = ", \n")

        navigateToTripButton.setOnClickListener { v ->
            val mapAreaString = mapAreaNameEditText.text.toString()
            if ("map_area_${mapAreaString}.sqlite" in mapArchiveFilesPath) { //TODO: FIX
                findNavController().navigate(
                    DownloadedTilesFragmentDirections.actionDownloadedTilesFragmentToTripFragment(mapAreaString)
                )
            } else {
                Toast.makeText(context, "No such MapArea", Toast.LENGTH_LONG).show()
            }
        }
    }

}