package com.example.sheeptracker.ui.tripdetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.TripDetailsFragmentBinding

class TripDetailsFragment : Fragment() {

    lateinit var binding: TripDetailsFragmentBinding

    val viewModel: TripDetailsViewModel by viewModels {
        TripDetailsViewModelFactory(
            TripDetailsFragmentArgs.fromBundle(requireArguments()).tripId,
            requireNotNull(activity).application,
            AppDatabase.getInstance(requireNotNull(activity).application).appDatabaseDao
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.trip_details_fragment, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.mapButton.setOnClickListener {
            findNavController().navigate(
                TripDetailsFragmentDirections.actionTripDetailsFragmentToTripFragment(
                    TripDetailsFragmentArgs.fromBundle(requireArguments()).tripId
                )
            )
        }

        return binding.root
    }

}