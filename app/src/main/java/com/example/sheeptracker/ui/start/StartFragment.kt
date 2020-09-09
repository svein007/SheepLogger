package com.example.sheeptracker.ui.start

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.databinding.StartFragmentBinding

class StartFragment : Fragment() {

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}