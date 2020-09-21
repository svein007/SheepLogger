package com.example.sheeptracker.ui.observationdetails

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
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

    private val SELECT_IMG_RES_INTENT_CODE = 1

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
        val viewModelFactory = ObservationDetailsViewModelFactory(arguments.observationId, application, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory)[ObservationDetailsViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val counterAdapter = CounterAdapter(
            CounterListItemListener {
                it.inc()
                binding.counterRV.adapter?.notifyDataSetChanged()
            },
            CounterListItemListener {
                it.dec()
                binding.counterRV.adapter?.notifyDataSetChanged()
            }
        )

        binding.counterRV.adapter = counterAdapter

        viewModel.counters.observe(viewLifecycleOwner, {
            it?.let {
                counterAdapter.submitList(it)
            }
        })

        binding.counterRV.addItemDecoration(DividerItemDecoration(application, DividerItemDecoration.VERTICAL))

        viewModel.observation.observe(viewLifecycleOwner) {
            binding.animalRegistrationIcon.setImageDrawable(it.observationType.getDrawable(resources))
        }

        val imagesAdapter = ImageResourceAdapter(
            ImgResourceListItemListener {
                Toast.makeText(requireContext(), "HEY: ${it}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.imagesRV.adapter = imagesAdapter

        viewModel.imageResources.observe(viewLifecycleOwner) {
            it?.let {
                imagesAdapter.submitList(it)
            }
        }

        binding.imagesRV.addItemDecoration(DividerItemDecoration(application, DividerItemDecoration.VERTICAL))

        binding.addImageButton.setOnClickListener {
            addImgRes()
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SELECT_IMG_RES_INTENT_CODE -> {
                    data?.let {
                        val selectedImageUri = it.data
                        viewModel.addImageResource(selectedImageUri.toString())
                    }
                }
            }
        }
    }


    /** Helpers **/

    private fun addImgRes() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select an image"), SELECT_IMG_RES_INTENT_CODE)
    }


}