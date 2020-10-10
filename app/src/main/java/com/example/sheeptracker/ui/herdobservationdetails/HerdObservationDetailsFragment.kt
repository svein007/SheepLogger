package com.example.sheeptracker.ui.herdobservationdetails

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.HerdObservationDetailsFragmentBinding
import com.example.sheeptracker.map.MapAreaManager
import com.example.sheeptracker.ui.addobservation.CounterAdapter
import com.example.sheeptracker.ui.addobservation.CounterListItemListener
import com.google.android.material.snackbar.Snackbar

class HerdObservationDetailsFragment : Fragment() {

    private lateinit var viewModel: HerdObservationDetailsViewModel
    private lateinit var binding: HerdObservationDetailsFragmentBinding
    private lateinit var arguments: HerdObservationDetailsFragmentArgs
    private lateinit var appDao: AppDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.herd_observation_details_fragment, container, false
        )

        arguments = HerdObservationDetailsFragmentArgs.fromBundle(requireArguments())

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application

        appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = HerdObservationDetailsViewModelFactory(arguments.observationId, application, appDao)

        viewModel = ViewModelProvider(
            requireActivity(), viewModelFactory
        )[HerdObservationDetailsViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val counterAdapter = CounterAdapter(
            CounterListItemListener {
                it.inc()
                binding.counterRV.adapter?.notifyDataSetChanged()
                viewModel.onUpdateCounter(it)
            },
            CounterListItemListener {
                it.dec()
                binding.counterRV.adapter?.notifyDataSetChanged()
                viewModel.onUpdateCounter(it)
            }
        )

        binding.counterRV.adapter = counterAdapter

        viewModel.counters.observe(viewLifecycleOwner, {
            it?.let {
                counterAdapter.submitList(it)
            }
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.herd_details_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.forEachIndexed { index, item ->
            if (item.itemId == R.id.mi_delete_secondary_trip_map_point) {
                item.isVisible = viewModel.observation.value?.observationSecondaryTripMapPointId != null
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_swiper -> {
                findNavController().navigate(
                    HerdObservationDetailsFragmentDirections.actionAnimalCountersDetailsFragmentToSwiperFragment()
                )
                return true
            }
            R.id.mi_delete_herd_obs -> {
                showDeleteObservationDialog()
                return true
            }
            R.id.mi_set_secondary_trip_map_point -> {
                addSecondaryTripMapPoint()
                return true
            }
            R.id.mi_delete_secondary_trip_map_point -> {
                viewModel.onDeleteSecondaryTripMapPoint()
                Snackbar
                    .make(binding.root, getString(R.string.secondary_trip_map_point_added), Snackbar.LENGTH_LONG)
                    .show()
                return true
            }
        }

        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onUpdateObservation()
        requireActivity().viewModelStore.clear() // DANGEROUS??
    }

    /** Helpers **/

    private fun showDeleteObservationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_observation))
            .setMessage(getString(R.string.delete_observation_query))
            .setPositiveButton(getString(R.string.delete)) { dialog, which ->
                viewModel.onDeleteObservation()
                findNavController().navigateUp()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            }
            .show()
    }

    private fun addSecondaryTripMapPoint() {
        val currentPosition = MapAreaManager.getLastKnownLocation(requireContext(), requireActivity(), 0, false)
        if (currentPosition != null) {
            viewModel.onAddSecondaryTripMapPoint(currentPosition)
        }
        Snackbar
            .make(binding.root, getString(R.string.secondary_trip_map_point_added), Snackbar.LENGTH_LONG)
            .show()
    }

}