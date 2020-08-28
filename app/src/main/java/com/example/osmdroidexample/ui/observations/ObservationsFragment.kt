package com.example.osmdroidexample.ui.observations

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.osmdroidexample.R
import com.example.osmdroidexample.database.AppDatabase
import com.example.osmdroidexample.databinding.ObservationsFragmentBinding

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
        //binding.viewModel = viewModel

        val adapter = ObservationAdapter(ObservationListItemListener { observationId ->
            Toast.makeText(requireContext(), "ObsID: $observationId", Toast.LENGTH_SHORT).show()
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