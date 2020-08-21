package com.example.osmdroidexample.ui.mapareas

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.osmdroidexample.R
import com.example.osmdroidexample.database.AppDatabase
import com.example.osmdroidexample.databinding.MapAreasFragmentBinding

class MapAreasFragment : Fragment() {

    private lateinit var viewModel: MapAreasViewModel
    private lateinit var binding: MapAreasFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.map_areas_fragment, container, false
        )

        val application = requireNotNull(this.activity).application
        val appDao = AppDatabase.getInstance(application).appDatabaseDao

        val viewModelFactory = MapAreasViewModelFactory(application, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[MapAreasViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.downliadedTilesViewModel = viewModel

        val adapter = MapAreaAdapter(MapAreaListItemListener {
            findNavController().navigate(
                MapAreasFragmentDirections.actionDownloadedTilesFragmentToTripFragment(it)
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

}