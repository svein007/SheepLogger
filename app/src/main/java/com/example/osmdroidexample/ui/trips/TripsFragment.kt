package com.example.osmdroidexample.ui.trips

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.osmdroidexample.R
import com.example.osmdroidexample.database.AppDatabase
import com.example.osmdroidexample.databinding.TripsFragmentBinding

class TripsFragment : Fragment() {

    private lateinit var viewModel: TripsViewModel
    private lateinit var binding: TripsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.trips_fragment, container, false
        )

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        val appDao = AppDatabase.getInstance(application).appDatabaseDao

        val viewModelFactory = TripsViewModelFactory(application, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[TripsViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = TripAdapter(TripListItemListener { tripId ->
            viewModel.trips.value?.firstOrNull { trip -> trip.tripId == tripId }?.tripOwnerMapAreaId?.let {mapAreaId ->
                findNavController().navigate(
                    TripsFragmentDirections.actionTripsFragmentToTripFragment(tripId, mapAreaId)
                )
            }
        })

        binding.tripsRecyclerView.adapter = adapter

        binding.tripsRecyclerView.addItemDecoration(DividerItemDecoration(application, DividerItemDecoration.VERTICAL))

        viewModel.trips.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.trips_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_add_trips) {
            findNavController().navigate(
                TripsFragmentDirections.actionTripsFragmentToAddTripFragment()
            )
            return true
        }
        return false
    }

}