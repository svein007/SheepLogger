package com.example.sheeptracker.ui.animalregistrationdetails

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.AnimalRegistrationDetailsFragmentBinding
import com.example.sheeptracker.utils.createImageFile
import java.io.File
import java.io.IOException
import java.lang.Exception

class AnimalRegistrationDetailsFragment : Fragment() {

    private val SELECT_IMG_RES_INTENT_CODE = 1
    private val TAKE_IMG_INTENT_CODE = 2

    private lateinit var viewModel: AnimalRegistrationDetailsViewModel
    private lateinit var binding: AnimalRegistrationDetailsFragmentBinding
    private lateinit var arguments: AnimalRegistrationDetailsFragmentArgs

    var currentPhotoPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.animal_registration_details_fragment, container, false
        )

        arguments = AnimalRegistrationDetailsFragmentArgs.fromBundle(requireArguments())

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = AnimalRegistrationDetailsViewModelFactory(arguments.observationId, application, appDao)

        viewModel = ViewModelProvider(this, viewModelFactory)[AnimalRegistrationDetailsViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.observation.observe(viewLifecycleOwner) {
            it?.let {
                binding.animalRegistrationIcon.setImageDrawable(it.observationType.getDrawable(resources))
            }
        }

        val imagesAdapter = ImageResourceAdapter(
            ImgResourceListItemListener { imgResId: Long, imgResUri: String ->
                findNavController().navigate(
                    AnimalRegistrationDetailsFragmentDirections.actionAnimalRegistrationDetailsFragmentToImageResourceFragment(imgResId, imgResUri)
                )
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
            showAddImageDialog()
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
                        viewModel.addImageResource(selectedImageUri.toString())
                    }
                }
                TAKE_IMG_INTENT_CODE -> {
                    currentPhotoPath?.also {
                        val drawable = Drawable.createFromPath(it)
                        viewModel.addImageResource(drawable!!)
                        try {
                            File(it).delete()
                            currentPhotoPath = null
                        } catch (e: Exception) {}
                    }
                }
            }
        }
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
        } else if (item.itemId == R.id.mi_delete_observation) {
            viewModel.deleteObservation()
            findNavController().navigateUp()
            return true
        }

        return false
    }

    /** Helpers **/

    private fun dispatchChooseImageIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), SELECT_IMG_RES_INTENT_CODE)
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
                setTitle(getString(R.string.add_image))
                setItems(arrayOf(getString(R.string.take_image), getString(R.string.choose_from_gallery))){ dialogInterface, index ->
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