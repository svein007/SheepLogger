package com.example.osmdroidexample.ui.addtrip

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.osmdroidexample.R
import com.example.osmdroidexample.database.AppDatabase
import com.example.osmdroidexample.database.entities.Trip
import com.example.osmdroidexample.databinding.AddTripFragmentBinding
import com.example.osmdroidexample.utils.dateToFormattedString
import com.example.osmdroidexample.utils.getToday

class AddTripFragment : Fragment() {

    private lateinit var viewModel: AddTripViewModel
    private lateinit var binding: AddTripFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.add_trip_fragment, container, false
        )

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        val appDao = AppDatabase.getInstance(application).appDatabaseDao

        val viewModelFactory = AddTripViewModelFactory(application, appDao)

        viewModel = ViewModelProvider(this,
            viewModelFactory)[AddTripViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_trip_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_add_trip -> {
                addTrip()
                return true
            }
        }
        return false
    }

    private fun addTrip() {
        viewModel.addTrip(
            onSuccess = { findNavController().popBackStack() },
            onFail = { Toast.makeText(requireContext(), "Invalid MapAreaId", Toast.LENGTH_LONG).show() }
        )
    }

}