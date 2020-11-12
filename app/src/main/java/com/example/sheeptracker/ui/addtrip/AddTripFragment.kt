package com.example.sheeptracker.ui.addtrip

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.AddTripFragmentBinding

class AddTripFragment : Fragment() {

    private lateinit var viewModel: AddTripViewModel
    private lateinit var binding: AddTripFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.add_trip_fragment, container, false
        )

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        val appDao = AppDatabase.getInstance(application).appDatabaseDao

        val viewModelFactory = AddTripViewModelFactory(application, appDao)

        viewModel = ViewModelProvider(this,
            viewModelFactory)[AddTripViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = SelectableMapAreaAdapter(SelectableMapAreaListItemListener {
            viewModel.mapAreaId.value = it.toString()
        })

        binding.mapAreasRV.adapter = adapter

        viewModel.mapAreas.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
                if (viewModel.mapAreaId.value.isNullOrBlank()) {
                    viewModel.mapAreaId.value = it.first().mapAreaId.toString()
                    adapter.notifyItemChanged(0)
                }
            }
        })

        binding.mapAreasRV.addItemDecoration(DividerItemDecoration(application, DividerItemDecoration.VERTICAL))

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_trip_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_add_trip -> {
                addTrip()
                return true
            }
        }
        return false
    }

    private fun addTrip() {
        if (viewModel.tripName.value.isNullOrBlank()) {
            Toast.makeText(requireContext(), getString(R.string.please_enter_trip_name), Toast.LENGTH_LONG).show()
        } else if (viewModel.mapAreas.value?.any { mapArea -> mapArea.mapAreaId.toString() == viewModel.mapAreaId.value } == false) {
            Toast.makeText(requireContext(), getString(R.string.please_select_a_maparea), Toast.LENGTH_LONG).show()
        } else {
            viewModel.addTrip(
                onSuccess = { tripId ->
                    findNavController().navigate(AddTripFragmentDirections.actionAddTripFragmentToTripFragment(tripId))
                },
                onFail = { }
            )
        }
    }

}