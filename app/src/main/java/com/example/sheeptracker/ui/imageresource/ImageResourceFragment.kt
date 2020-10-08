package com.example.sheeptracker.ui.imageresource

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.ImageResourceFragmentBinding

class ImageResourceFragment : Fragment() {

    private lateinit var viewModel: ImageResourceViewModel
    private lateinit var binding: ImageResourceFragmentBinding
    private lateinit var arguments: ImageResourceFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.image_resource_fragment, container, false
        )

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        val appDao = AppDatabase.getInstance(application).appDatabaseDao

        arguments = ImageResourceFragmentArgs.fromBundle(requireArguments())

        val viewModelFactory = ImageResourceViewModelFactory(application, appDao, arguments.imageResourceId, arguments.imageUri)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[ImageResourceViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.image_resource_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_delete_image) {
            showDeleteImageDialog()
            return true
        }
        return false
    }

    /** Helpers **/

    private fun showDeleteImageDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_image))
            .setMessage(getString(R.string.delete_image_query))
            .setPositiveButton(getString(R.string.delete)) { dialog, which ->
                viewModel.deleteImage()
                findNavController().navigateUp()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            }
            .show()
    }

}