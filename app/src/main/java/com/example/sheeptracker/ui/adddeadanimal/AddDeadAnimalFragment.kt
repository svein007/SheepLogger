package com.example.sheeptracker.ui.adddeadanimal

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.TripMapPoint
import com.example.sheeptracker.databinding.AddDeadAnimalFragmentBinding
import com.example.sheeptracker.map.MapAreaManager
import java.util.*

class AddDeadAnimalFragment : Fragment() {

    private lateinit var viewModel: AddDeadAnimalViewModel
    private lateinit var binding: AddDeadAnimalFragmentBinding
    private lateinit var arguments: AddDeadAnimalFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.add_dead_animal_fragment, container, false
        )

        arguments = AddDeadAnimalFragmentArgs.fromBundle(requireArguments())

        val currentPosition = MapAreaManager.getLastKnownLocation(
            requireContext(),
            requireActivity(),
            1
        )

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = AddDeadAnimalViewModelFactory(
            arguments.tripId,
            TripMapPoint(
                tripMapPointLat =  currentPosition!!.latitude,
                tripMapPointLon =  currentPosition!!.longitude,
                tripMapPointDate = Date(),
                tripMapPointOwnerTripId = arguments.tripId
            ),
            enumValues<Observation.ObservationType>()[arguments.obsType],
            application,
            appDao
        )

        viewModel = ViewModelProvider(
            this,
            viewModelFactory)[AddDeadAnimalViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.button.setOnClickListener {
            saveObservation()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

    private fun saveObservation() {
        viewModel.addObservation(
            lat = arguments.obsLat.toDouble(),
            lon = arguments.obsLon.toDouble(),
            onSuccess = { findNavController().popBackStack() },
            onFail = { Log.d("#####", "Can't create observation in DB") }
        )
    }

}