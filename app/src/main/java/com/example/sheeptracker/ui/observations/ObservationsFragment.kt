package com.example.sheeptracker.ui.observations

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.ObservationsFragmentBinding

class ObservationsFragment : Fragment() {

    private lateinit var viewModel: ObservationsViewModel
    private lateinit var binding: ObservationsFragmentBinding
    private lateinit var arguments: ObservationsFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.observations_fragment, container, false
        )

        arguments = ObservationsFragmentArgs.fromBundle(requireArguments())

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = ObservationsViewModelFactory(arguments.tripId, appDao, application)

        viewModel = ViewModelProvider(
            this, viewModelFactory)[ObservationsViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val adapter = ObservationAdapter(ObservationListItemListener { observationId ->
            findNavController().navigate(
                ObservationsFragmentDirections.actionObservationsFragmentToObservationDetailsFragment(observationId)
            )
        })

        binding.observationsRV.adapter = adapter

        viewModel.observations.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}