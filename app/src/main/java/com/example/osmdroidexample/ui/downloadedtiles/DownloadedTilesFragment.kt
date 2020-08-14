package com.example.osmdroidexample.ui.downloadedtiles

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.osmdroidexample.R
import kotlinx.android.synthetic.main.downloaded_tiles_fragment.*

class DownloadedTilesFragment : Fragment() {

    companion object {
        fun newInstance() =
            DownloadedTilesFragment()
    }

    private lateinit var viewModel: DownloadedTilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.downloaded_tiles_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DownloadedTilesViewModel::class.java)
        // TODO: Use the ViewModel

        val mapArchiveFilesPath = context?.databaseList() // All DB-Files

        Log.d("########", mapArchiveFilesPath?.get(0).toString())

        mapAreaFilesTextView.text = mapArchiveFilesPath?.joinToString(separator = ", ") ?: ""
    }

}