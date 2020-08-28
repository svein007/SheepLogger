package com.example.osmdroidexample.ui.trips

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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

        val application = requireNotNull(this.activity).application
        val appDao = AppDatabase.getInstance(application).appDatabaseDao

        val viewModelFactory = TripsViewModelFactory(application, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[TripsViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = TripAdapter(TripListItemListener { tripId ->
            Toast.makeText(requireContext(), "Trip ID: $tripId", Toast.LENGTH_SHORT).show()

            viewModel.trips.value?.firstOrNull { trip -> trip.tripId == tripId }?.tripOwnerMapAreaId?.let {mapAreaId ->
                findNavController().navigate(
                    TripsFragmentDirections.actionTripsFragmentToTripFragment(tripId, mapAreaId)
                )
            }

        })

        binding.tripsRecyclerView.adapter = adapter

        viewModel.trips.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        binding.newTripButton.setOnClickListener {
            findNavController().navigate(
                TripsFragmentDirections.actionTripsFragmentToAddTripFragment()
            )
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}