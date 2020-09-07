package com.example.osmdroidexample.ui.addobservation

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.osmdroidexample.R
import com.example.osmdroidexample.database.AppDatabase
import com.example.osmdroidexample.database.entities.TripMapPoint
import com.example.osmdroidexample.databinding.AddObservationFragmentBinding
import com.example.osmdroidexample.map.MapAreaManager
import com.example.osmdroidexample.utils.dateToFormattedString
import com.example.osmdroidexample.utils.getToday

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

        val currentPosition = MapAreaManager.getLastKnownLocation(
            requireContext(),
            requireActivity(),
            1
            )

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = AddObservationViewModelFactory(
            arguments.tripId,
            TripMapPoint(
                tripMapPointLat =  currentPosition!!.latitude,
                tripMapPointLon =  currentPosition!!.longitude,
                tripMapPointDate = dateToFormattedString(getToday()),
                tripMapPointOwnerTripId = arguments.tripId
                )
            , application
            , appDao)

        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory)[AddObservationViewModel::class.java]

        Log.d("####", "AVM tripId: ${viewModel.trip.value?.tripId}")

        binding.swiperButton.setOnClickListener {
            findNavController().navigate(
                AddObservationFragmentDirections.actionAddObservationFragmentToSwiperFragment()
            )
        }

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val adapter = CounterAdapter(
            CounterListItemListener {
                it.inc()
                binding.counterRV.adapter?.notifyDataSetChanged()
            },
            CounterListItemListener {
                it.dec()
                binding.counterRV.adapter?.notifyDataSetChanged()
            }
        )

        binding.counterRV.adapter = adapter

        viewModel.counters.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        binding.counterRV.addItemDecoration(DividerItemDecoration(application, DividerItemDecoration.VERTICAL))

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().viewModelStore.clear() // DANGEROUS??
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_observation_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_add_observation) {
            saveObservation()
            return true
        }

        return false
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