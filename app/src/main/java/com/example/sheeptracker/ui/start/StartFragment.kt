package com.example.sheeptracker.ui.start

import android.Manifest
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.databinding.StartFragmentBinding

class StartFragment : Fragment() {

    private val requestCode = 1

    private lateinit var viewModel: StartViewModel
    private lateinit var binding: StartFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.start_fragment, container, false
        )

        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this)[StartViewModel::class.java]

        binding.offlineMapAreasButton.setOnClickListener {
            findNavController().navigate(
                StartFragmentDirections.actionStartFragmentToMapAreasFragment()
            )
        }

        binding.tripsButton.setOnClickListener {
            findNavController().navigate(
                StartFragmentDirections.actionStartFragmentToTripsFragment()
            )
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.start_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_request_permissions) {
            performRequestPermissions()
            return true
        }
        return false
    }

    private fun performRequestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            requestCode)
    }

}