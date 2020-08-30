package com.example.osmdroidexample.ui.addobservation

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.osmdroidexample.R
import com.example.osmdroidexample.database.AppDatabase
import com.example.osmdroidexample.databinding.AddObservationFragmentBinding

class AddObservationFragment : Fragment() {

    private lateinit var viewModel: AddObservationViewModel
    private lateinit var binding: AddObservationFragmentBinding
    private lateinit var arguments: AddObservationFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.add_observation_fragment, container, false
        )

        setHasOptionsMenu(true)

        arguments = AddObservationFragmentArgs.fromBundle(requireArguments())

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = AddObservationViewModelFactory(arguments.tripId, application, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory)[AddObservationViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_observation_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_add_observation) {
            viewModel.addObservation(
                onSuccess = { findNavController().popBackStack() },
                onFail = { }
            )
            return true
        }

        return false
    }

}