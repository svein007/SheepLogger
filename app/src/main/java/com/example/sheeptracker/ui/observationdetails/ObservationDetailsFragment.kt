package com.example.sheeptracker.ui.observationdetails

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.ObservationDetailsFragmentBinding
import com.example.sheeptracker.ui.addobservation.CounterAdapter
import com.example.sheeptracker.ui.addobservation.CounterListItemListener

class ObservationDetailsFragment : Fragment() {

    private lateinit var viewModel: ObservationDetailsViewModel
    private lateinit var binding: ObservationDetailsFragmentBinding
    private lateinit var arguments: ObservationDetailsFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.observation_details_fragment, container, false
        )

        arguments = ObservationDetailsFragmentArgs.fromBundle(requireArguments())

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = ObservationDetailsViewModelFactory(arguments.observationId, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory)[ObservationDetailsViewModel::class.java]

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.observation_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.mi_observation_details_save) {
            viewModel.onUpdateObservation()
            findNavController().navigateUp()
            return true
        }

        return false
    }

}