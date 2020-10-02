package com.example.sheeptracker.ui.addanimalregistration

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.TripMapPoint
import com.example.sheeptracker.databinding.AddAnimalRegistrationFragmentBinding
import com.example.sheeptracker.map.MapAreaManager
import com.example.sheeptracker.ui.animalregistrationdetails.ImageResourceAdapter
import com.example.sheeptracker.ui.animalregistrationdetails.ImgResourceListItemListener
import com.example.sheeptracker.utils.createImageFile
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

class AddAnimalRegistrationFragment : Fragment() {

    private val SELECT_IMG_RES_INTENT_CODE = 1
    private val TAKE_IMG_INTENT_CODE = 2

    private lateinit var registrationViewModel: AddAnimalRegistrationViewModel
    private lateinit var binding: AddAnimalRegistrationFragmentBinding
    private lateinit var arguments: AddAnimalRegistrationFragmentArgs

    var currentPhotoPath: String? = null

    private var shouldDeleteObservation = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.add_animal_registration_fragment, container, false
        )

        arguments = AddAnimalRegistrationFragmentArgs.fromBundle(requireArguments())

        val currentPosition = MapAreaManager.getLastKnownLocation(
            requireContext(),
            requireActivity(),
            1
        )

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = AddAnimalRegistrationViewModelFactory(
            arguments.tripId,
            arguments.obsLat.toDouble(),
            arguments.obsLon.toDouble(),
            TripMapPoint(
                tripMapPointLat =  currentPosition!!.latitude,
                tripMapPointLon =  currentPosition!!.longitude,
                tripMapPointDate = Date(),
                tripMapPointOwnerTripId = arguments.tripId
            ),
            enumValues<Observation.ObservationType>()[arguments.obsType],
            application,
            appDao
        )

        registrationViewModel = ViewModelProvider(
            this,
            viewModelFactory)[AddAnimalRegistrationViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = registrationViewModel

        val imagesAdapter = ImageResourceAdapter(
            ImgResourceListItemListener { imgResId: Long, imgResUri: String ->
                findNavController().navigate(
                    AddAnimalRegistrationFragmentDirections.actionAddDeadAnimalFragmentToImageResourceFragment(imgResId, imgResUri)
                )
            }
        )

        binding.imagesRV.adapter = imagesAdapter

        registrationViewModel.imageResources.observe(viewLifecycleOwner) {
            it?.let {
                imagesAdapter.submitList(it)
            }
        }

        binding.imagesRV.addItemDecoration(DividerItemDecoration(application, DividerItemDecoration.VERTICAL))

        binding.button.setOnClickListener {
            shouldDeleteObservation = false
            registrationViewModel.updateObservation()
            findNavController().navigateUp()
        }

        binding.addImageButton.setOnClickListener {
            showAddImageDialog()
        }

        registrationViewModel.observation.observe(viewLifecycleOwner){
            binding.animalRegistrationIcon.setImageDrawable(it.observationType.getDrawable(resources))
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SELECT_IMG_RES_INTENT_CODE -> {
                    data?.let {
                        val selectedImageUri = it.data
                        registrationViewModel.addImageResource(selectedImageUri.toString())
                    }
                }
                TAKE_IMG_INTENT_CODE -> {
                    currentPhotoPath?.also {
                        val drawable = Drawable.createFromPath(it)
                        registrationViewModel.addImageResource(drawable!!)
                        try {
                            File(it).delete()
                            currentPhotoPath = null
                        } catch (e: Exception) {}
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registrationViewModel.refreshImageResources()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (shouldDeleteObservation) {
            registrationViewModel.deleteObservation()
        }
    }

    /** Helpers **/

    private fun dispatchChooseImageIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select an image"), SELECT_IMG_RES_INTENT_CODE)
    }

    private fun dispatchTakeImageIntent(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile(requireContext())
                } catch (ex: IOException) {
                    null
                }

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.android.fileprovider",
                        it
                    )
                    currentPhotoPath = it.absolutePath
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, TAKE_IMG_INTENT_CODE)
                }
            }
        }
    }

    private fun showAddImageDialog() {
        val observationTypeAlertDialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Add image")
                setItems(arrayOf("Take image", "Choose from gallery")){ dialogInterface, index ->
                    when (index) {
                        0 -> {
                            dispatchTakeImageIntent()
                        }
                        1 -> {
                            dispatchChooseImageIntent()
                        }
                    }
                }
            }
            builder.create()
        }

        observationTypeAlertDialog?.show()
    }

}