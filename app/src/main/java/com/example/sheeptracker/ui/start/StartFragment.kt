package com.example.sheeptracker.ui.start

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.StartFragmentBinding
import com.example.sheeptracker.map.MapAreaManager
import com.example.sheeptracker.utils.checkHasAllPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StartFragment : Fragment() {

    private val requestCodeMapAreas = 1
    private val requestCodeTrips = 2
    private val requestCodeAddTrip = 3
    private val requestCodeTrip = 4

    private var toast: Toast? = null

    private val viewModel: StartViewModel by viewModels {
        StartViewModelFactory(AppDatabase.getInstance(requireContext().applicationContext).appDatabaseDao, requireActivity().application)
    }
    private lateinit var binding: StartFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.start_fragment, container, false
        )

        //HACK: try to prepare GPS location
        MapAreaManager.getLastKnownLocation(requireContext(), requireActivity(), 0, false)

        //HACK: populate mapAreaCount
        viewModel.mapAreaCount.observe(viewLifecycleOwner) {
            it
        }

        binding.startViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.startTripConstraintLayout.setOnClickListener {
            if (checkHasAllPermissions(requireContext())) {
                if (viewModel.tripIsActive.value!!) {
                    findNavController().navigate(
                        StartFragmentDirections.actionStartFragmentToTripFragment(
                            viewModel.activeTrip.value!!.tripId
                        )
                    )
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        val mapAreaCount = withContext(Dispatchers.IO) {
                            AppDatabase.getInstance(requireContext()).appDatabaseDao.getMapAreaCount()
                        }
                        if (mapAreaCount > 0) {
                            findNavController().navigate(
                                StartFragmentDirections.actionStartFragmentToAddTripFragment()
                            )
                        } else {
                            toast?.cancel()
                            toast = Toast.makeText(requireContext(), getString(R.string.no_map_areas_query), Toast.LENGTH_LONG)
                            toast?.show()
                        }
                    }
                }
            } else {
                performRequestPermissions(
                    if (viewModel.tripIsActive.value!!) requestCodeTrip else requestCodeAddTrip)
            }
        }

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
                requestCodeAddTrip -> {
                    findNavController().navigate(
                        StartFragmentDirections.actionStartFragmentToAddTripFragment()
                    )
                }
                requestCodeTrip -> {
                    findNavController().navigate(
                        StartFragmentDirections.actionStartFragmentToTripFragment(
                            viewModel.activeTrip.value!!.tripId
                        )
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