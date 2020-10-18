package com.example.sheeptracker.ui.start

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.databinding.StartFragmentBinding
import com.example.sheeptracker.utils.checkHasAllPermissions

class StartFragment : Fragment() {

    private val requestCodeMapAreas = 1
    private val requestCodeTrips = 2

    private lateinit var viewModel: StartViewModel
    private lateinit var binding: StartFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.start_fragment, container, false
        )

        viewModel = ViewModelProvider(this)[StartViewModel::class.java]

        binding.mapAreasConstraintLayout.setOnClickListener {
            if (checkHasAllPermissions(requireContext())) {
                findNavController().navigate(
                    StartFragmentDirections.actionStartFragmentToMapAreasFragment()
                )
            } else {
                performRequestPermissions(requestCodeMapAreas)
            }
        }

        binding.tripsConstraintLayout.setOnClickListener {
            if (checkHasAllPermissions(requireContext())) {
                findNavController().navigate(
                    StartFragmentDirections.actionStartFragmentToTripsFragment()
                )
            } else {
                performRequestPermissions(requestCodeTrips)
            }
        }

        binding.rapportConstraintLayout.setOnClickListener {
            if (checkHasAllPermissions(requireContext())) {
                findNavController().navigate(
                    StartFragmentDirections.actionStartFragmentToSimpleRapportDialog()
                )
            }
        }

        binding.settingsConstraintLayout.setOnClickListener {
            findNavController().navigate(
                StartFragmentDirections.actionStartFragmentToSettingsFragment()
            )
        }

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty()
            && grantResults.all { grantResult -> grantResult == PackageManager.PERMISSION_GRANTED}) {
            when (requestCode) {
                requestCodeMapAreas -> {
                    findNavController().navigate(
                        StartFragmentDirections.actionStartFragmentToMapAreasFragment()
                    )
                }
                requestCodeTrips -> {
                    findNavController().navigate(
                        StartFragmentDirections.actionStartFragmentToTripsFragment()
                    )
                }
            }
        } else {
            Toast.makeText(requireContext(), "Need permissions to continue", Toast.LENGTH_LONG).show()
        }
    }

    private fun performRequestPermissions(requestCode: Int) {
        if (checkHasAllPermissions(requireContext())) {
            Toast.makeText(requireContext(), "Permissions already granted", Toast.LENGTH_LONG).show()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                requestCode)
        }

    }

}