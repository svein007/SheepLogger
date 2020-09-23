package com.example.sheeptracker.ui.mapareas

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.MapAreasFragmentBinding

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

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        val appDao = AppDatabase.getInstance(application).appDatabaseDao

        val viewModelFactory = MapAreasViewModelFactory(application, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[MapAreasViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.mapAreasViewModel = viewModel

        val adapter = MapAreaAdapter(MapAreaListItemListener {
            findNavController().navigate(
                MapAreasFragmentDirections.actionMapAreasFragmentToMapAreaFragment(it)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.map_areas_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_add_map_area) {
            findNavController().navigate(
                MapAreasFragmentDirections.actionMapAreasFragmentToMainFragment()
            )
            return true
        }
        return false
    }

}