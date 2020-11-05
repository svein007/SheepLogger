package com.example.sheeptracker.ui.observations

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.databinding.ObservationsFragmentBinding

class ObservationsFragment : Fragment() {

    private lateinit var viewModel: ObservationsViewModel
    private lateinit var binding: ObservationsFragmentBinding
    private lateinit var arguments: ObservationsFragmentArgs

    private lateinit var adapter: ObservationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.observations_fragment, container, false
        )

        setHasOptionsMenu(true)

        arguments = ObservationsFragmentArgs.fromBundle(requireArguments())

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = ObservationsViewModelFactory(arguments.tripId, appDao, application)

        viewModel = ViewModelProvider(
            this, viewModelFactory)[ObservationsViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        adapter = ObservationAdapter(application, ObservationListItemListener { observationId, observationType ->

            when (observationType) {
                Observation.ObservationType.COUNT -> {
                    findNavController().navigate(
                        ObservationsFragmentDirections.actionObservationsFragmentToAnimalCountersDetailsFragment(observationId)
                    )
                }
                else -> {
                    findNavController().navigate(
                        ObservationsFragmentDirections.actionObservationsFragmentToAnimalRegistrationDetailsFragment(observationId)
                    )
                }
            }

        })

        binding.observationsRV.adapter = adapter

        viewModel.observations.observe(viewLifecycleOwner, {
            it?.let {
                //TODO: Filter here based on filter-value
                //adapter.submitList(it)
                updateAdapter(it, viewModel.filter.value)
            }
        })

        viewModel.filter.observe(viewLifecycleOwner) {
            updateAdapter(viewModel.observations.value, it)
        }

        binding.observationsRV.addItemDecoration(DividerItemDecoration(application, DividerItemDecoration.VERTICAL))

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.observations_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_filter_observations) {
            viewModel.nextFilter()
            return true
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

    private fun updateAdapter(observations: List<Observation>?, filter: Observation.ObservationType?) {
        if (observations != null) {
            if (filter == null) {
                adapter.submitList(observations)
            } else {
                val filteredObservations = observations.filter { obs -> obs.observationType == filter }
                adapter.submitList(filteredObservations)
            }
        }
    }

}