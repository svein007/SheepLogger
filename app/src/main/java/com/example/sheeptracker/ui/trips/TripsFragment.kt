package com.example.sheeptracker.ui.trips

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
import com.example.sheeptracker.database.AppDao
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.database.entities.Trip
import com.example.sheeptracker.databinding.TripsFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

class TripsFragment : Fragment() {

    private lateinit var viewModel: TripsViewModel
    private lateinit var binding: TripsFragmentBinding

    private lateinit var appDao: AppDao
    private lateinit var adapter: TripAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.trips_fragment, container, false
        )

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        appDao = AppDatabase.getInstance(application).appDatabaseDao

        val viewModelFactory = TripsViewModelFactory(application, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[TripsViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        adapter = TripAdapter(TripListItemListener { tripId ->
            findNavController().navigate(
                TripsFragmentDirections.actionTripsFragmentToTripDetailsFragment(tripId)
            )
        })

        binding.tripsRecyclerView.adapter = adapter

        binding.tripsRecyclerView.addItemDecoration(DividerItemDecoration(application, DividerItemDecoration.VERTICAL))

        viewModel.trips.observe(viewLifecycleOwner, {
            it?.let {
                updateAdapter(it, viewModel.filter.value)
            }
        })

        viewModel.filter.observe(viewLifecycleOwner) {
            it?.let {
                updateAdapter(viewModel.trips.value, it)
            }
        }

        viewModel.filter.value?.let {
            updateTitlebar(if (it.first == Menu.FIRST) getString(R.string.my_trips) else it.second)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.trips_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_filter_trips) {
            showFilterPopup(requireActivity().findViewById(R.id.mi_filter_trips))
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showFilterPopup(v: View) {
        CoroutineScope(Dispatchers.Main).launch {
            val popup = PopupMenu(requireContext(), v)

            popup.menu.clear()
            popup.menu.add(Menu.FIRST, Menu.FIRST, Menu.FIRST, R.string.all).isCheckable = true

            val tripYears = getTripYears()

            for ((i, year) in tripYears.withIndex()) {
                popup.menu.add(Menu.FIRST, Menu.FIRST+1+i, Menu.FIRST+1+i, year).apply {
                    isCheckable = true
                }
            }

            popup.setOnMenuItemClickListener { item ->
                viewModel.filter.value = Pair(item.itemId, item.title.toString())

                when (item.itemId) {
                    Menu.FIRST -> {
                        updateTitlebar(getString(R.string.my_trips))
                        return@setOnMenuItemClickListener true
                    }
                    else -> {
                        updateTitlebar(item.title.toString())
                        return@setOnMenuItemClickListener true
                    }
                }
            }

            popup.menu.setGroupCheckable(Menu.FIRST, true, true)

            val checkedItemId = viewModel.filter.value?.first ?: 0
            popup.menu.findItem(checkedItemId)?.isChecked = true

            popup.show()
        }
    }

    private suspend fun getTripYears(): List<String> {
        return withContext(Dispatchers.IO) {
            appDao.getFinishedTripsAsc().map { trip ->
                val cal = Calendar.getInstance().apply{
                    time = trip.tripDate
                }
                cal.get(Calendar.YEAR).toString()
            }.distinct().sortedDescending()
        }
    }

    private fun updateTitlebar(title: String) {
        try {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = title
        } catch (e: Exception) {}
    }

    private fun updateAdapter(trips: List<Trip>?, filter: Pair<Int, String>?) {
        if (trips != null) {
            if (filter == null) {
                adapter.submitList(trips)
            } else {
                val filteredTrips = trips.filter { trip ->
                    val cal = Calendar.getInstance().apply{
                        time = trip.tripDate
                    }
                    val year = cal.get(Calendar.YEAR).toString()
                    filter.first == Menu.FIRST || year == filter.second
                }
                adapter.submitList(filteredTrips)
            }
        }
    }

}