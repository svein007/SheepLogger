package com.example.sheeptracker.ui.mapareadownload

import android.app.ActionBar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.databinding.NameMapAreaDialogBinding
import com.example.sheeptracker.map.MapAreaManager

class NameMapAreaDialog : DialogFragment() {

    lateinit var binding: NameMapAreaDialogBinding
    val viewModel: MapAreaDownloadViewModel by activityViewModels {
        MapAreaDownloadViewModelFactory(requireActivity().application, AppDatabase.getInstance(requireActivity().application).appDatabaseDao)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.name_map_area_dialog, container, false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.cancelButton.setOnClickListener { dismiss() }

        binding.downloadButton.setOnClickListener { storeMapArea() }

        binding.tileCountTV.text = "${viewModel.tileCount} tiles"

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
    }

    private fun storeMapArea() {
        val mapArea = MapArea(
            mapAreaName = viewModel.mapAreaName.value!!,
            mapAreaMinZoom = viewModel.minZoom,
            mapAreaMaxZoom = viewModel.maxZoom,
            boundingBox = viewModel.boundingBox!!
        )

        viewModel.storeMapArea(mapArea)

        MapAreaManager.storeMapArea(
            requireContext(),
            viewModel.boundingBox!!,
            viewModel.minZoom.toInt(),
            viewModel.maxZoom.toInt(),
            mapArea.getSqliteFilename()
        ) {
            findNavController().popBackStack(R.id.mapAreaDownloadFragment, true)
        }
    }

}