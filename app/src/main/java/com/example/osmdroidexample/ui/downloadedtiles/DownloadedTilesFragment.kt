package com.example.osmdroidexample.ui.downloadedtiles

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.osmdroidexample.R

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
    }

}