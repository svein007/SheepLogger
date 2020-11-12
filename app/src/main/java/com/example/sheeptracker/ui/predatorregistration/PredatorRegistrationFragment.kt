package com.example.sheeptracker.ui.predatorregistration

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
import androidx.core.view.forEachIndexed
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.PredatorRegistrationFragmentBinding
import com.example.sheeptracker.ui.animalregistrationdetails.ImageResourceAdapter
import com.example.sheeptracker.ui.animalregistrationdetails.ImgResourceListItemListener
import com.example.sheeptracker.utils.createImageFile
import java.io.File
import java.io.IOException
import java.lang.Exception

class PredatorRegistrationFragment : Fragment() {

    private val SELECT_IMG_RES_INTENT_CODE = 1
    private val TAKE_IMG_INTENT_CODE = 2

    private lateinit var binding: PredatorRegistrationFragmentBinding

    private val args: PredatorRegistrationFragmentArgs by navArgs()

    private val viewModel: PredatorRegistrationViewModel by viewModels{
        PredatorRegistrationViewModelFactory(
            obsId = args.obsId,
            tripId = args.tripId,
            obsLat = args.obsLat?.toDoubleOrNull() ?: -1.0,
            obsLon = args.obsLon?.toDoubleOrNull() ?: -1.0,
            application = requireActivity().application,
            appDao = AppDatabase.getInstance(requireContext()).appDatabaseDao
        )
    }

    private var shouldDeleteObservation = false

    var currentPhotoPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.predator_registration_fragment, container, false)

        setHasOptionsMenu(true)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.observation.observe(viewLifecycleOwner){
            it
        }

        val imagesAdapter = ImageResourceAdapter(
            ImgResourceListItemListener { imgResId: Long, imgResUri: String ->
                findNavController().navigate(
                    PredatorRegistrationFragmentDirections.actionPredatorRegistrationFragmentToImageResourceFragment(
                        imgResId,
                        imgResUri
                    )
                )
            }
        )

        binding.predatorsImageRV.adapter = imagesAdapter

        viewModel.imageResources.observe(viewLifecycleOwner) {
            it?.let {
                imagesAdapter.submitList(it)
            }
        }

        binding.predatorsImageRV.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        binding.addPredatorImageButton.setOnClickListener {
            showAddImageDialog()
        }

        shouldDeleteObservation = args.obsId < 0

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        if (shouldDeleteObservation) {
            viewModel.deleteObservation()
        }
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
        inflater.inflate(R.menu.predator_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.forEachIndexed { index, item ->
            when (item.itemId) {
                R.id.mi_add_predator -> {
                    item.title = if (args.obsId < 0) getString(R.string.add) else getString(R.string.save)
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_delete_predator -> {
                showDeletePredatorRegistrationDialog()
                return true
            }
            R.id.mi_add_predator -> {
                shouldDeleteObservation = false
                viewModel.updateObservation()
                findNavController().navigateUp()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    /** Helpers **/

    private fun showDeletePredatorRegistrationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_observation)
            .setMessage(R.string.delete_observation_query)
            .setPositiveButton(getString(R.string.delete)) { dialog, which ->
                viewModel.deleteObservation()
                findNavController().navigateUp()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            }
            .show()
    }

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
                        "com.example.sheeptracker.fileprovider",
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