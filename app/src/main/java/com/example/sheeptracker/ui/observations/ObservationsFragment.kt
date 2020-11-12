package com.example.sheeptracker.ui.observations

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.databinding.ObservationsFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

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
                Observation.ObservationType.PREDATOR, Observation.ObservationType.ENVIRONMENT -> {
                    findNavController().navigate(
                        ObservationsFragmentDirections.actionObservationsFragmentToPredatorRegistrationFragment(
                            null, null
                        ).setObsId(observationId).setObsType(observationType.ordinal)
                    )
                }
                Observation.ObservationType.DEAD, Observation.ObservationType.INJURED -> {
                    findNavController().navigate(
                        ObservationsFragmentDirections.actionObservationsFragmentToAnimalRegistrationDetailsFragment(observationId)
                    )
                }
            }

        })

        binding.observationsRV.adapter = adapter

        viewModel.observations.observe(viewLifecycleOwner, {
            it?.let {
                updateAdapter(it, viewModel.filter.value)
            }
        })

        viewModel.filter.observe(viewLifecycleOwner) {
            updateAdapter(viewModel.observations.value, it)
            updateTitlebar(it)
        }

        viewModel.filter.value?.let {
            updateTitlebar(it)
        }

        binding.observationsRV.addItemDecoration(DividerItemDecoration(application, DividerItemDecoration.VERTICAL))

        return binding.root
    }

    private fun updateTitlebar(title: String) {
        try {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = title
        } catch (e: Exception) {}
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.observations_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_filter_observations -> {
                showFilterPopup(requireActivity().findViewById(R.id.mi_filter_observations))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun showFilterPopup(v: View) {
        CoroutineScope(Dispatchers.Main).launch {
            val popup = PopupMenu(requireContext(), v)

            popup.menu.clear()
            popup.menu.add(Menu.FIRST, Menu.FIRST, Menu.FIRST, R.string.all).isCheckable = true

            val obsTypes = Observation.ObservationType.values()
            val obsTypeStrings = obsTypes.map { observationType -> observationType.getString(requireContext()) }

            for ((i, obsTypeStr) in obsTypeStrings.withIndex()) {
                popup.menu.add(Menu.FIRST, Menu.FIRST+1+i, Menu.FIRST+1+i, obsTypeStr).apply {
                    isCheckable = true
                }
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    Menu.FIRST -> {
                        viewModel.filter.value = null
                    }
                    else -> {
                        viewModel.filter.value = Observation.ObservationType.values()[item.itemId-2]
                    }
                }
                updateTitlebar(viewModel.filter.value)
                return@setOnMenuItemClickListener true
            }

            popup.menu.setGroupCheckable(Menu.FIRST, true, true)

            var checkedItemId = Menu.FIRST
            viewModel.filter.value?.ordinal?.let { checkedItemId += (it + 1) }
            popup.menu.findItem(checkedItemId)?.isChecked = true

            popup.show()
        }
    }

    private fun updateTitlebar(observationType: Observation.ObservationType?) {
        when (observationType) {
            null -> updateTitlebar(getString(R.string.observations))
            else -> updateTitlebar(observationType.getString(requireContext()))
        }
    }

}