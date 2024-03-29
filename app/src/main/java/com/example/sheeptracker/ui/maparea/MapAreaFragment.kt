package com.example.sheeptracker.ui.maparea

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.MapAreaFragmentBinding
import com.example.sheeptracker.utils.getObservationShortDesc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.lang.Exception

class MapAreaFragment : Fragment() {

    private lateinit var viewModel: MapAreaViewModel
    private lateinit var binding: MapAreaFragmentBinding

    private lateinit var arguments: MapAreaFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.map_area_fragment, container, false
        )

        arguments = MapAreaFragmentArgs.fromBundle(requireArguments())

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        val appDao = AppDatabase.getInstance(application).appDatabaseDao

        val viewModelFactory = MapAreaViewModelFactory(arguments.mapAreaId, application, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[MapAreaViewModel::class.java]

        viewModel.mapArea.observe(viewLifecycleOwner, {
            it?.let {
                Log.d("#######", "MapArea: " + it.toString())
                binding.mapAreaMapView.minZoomLevel = it.mapAreaMinZoom
                binding.mapAreaMapView.maxZoomLevel = it.mapAreaMaxZoom
                binding.mapAreaMapView.controller.zoomTo(it.mapAreaMinZoom)
                binding.mapAreaMapView.controller.animateTo(it.boundingBox.centerWithDateLine)

                val mapAreaString = it.getSqliteFilename()
                setupMapView(binding.mapAreaMapView, mapAreaString)
            }
        })

        viewModel.observations.observe(viewLifecycleOwner) {
            it?.let {
                binding.mapAreaMapView.drawObservationMarkers(it)
                CoroutineScope(Dispatchers.Main).launch {
                    val shortObservationDescriptions = ArrayList<String>()
                    viewModel.observations.value!!.forEachIndexed { i, observation ->
                        val observationShortDescription = getObservationShortDesc(appDao, binding.root.context, observation).replace("\n", "")
                        shortObservationDescriptions.add("\n${observationShortDescription}")
                    }
                    binding.mapAreaMapView.attachObservationMarkerSnippets(shortObservationDescriptions)
                }
            }
        }

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onPause() {
        super.onPause()
        binding.mapAreaMapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.mapAreaMapView.onResume()
    }

    private fun setupMapView(mapView: MapView, mapAreaName: String) {
        mapView.setUseDataConnection(false)
        mapView.isTilesScaledToDpi = true
        mapView.setMultiTouchControls(true)

        val mapAreaFile = context?.getDatabasePath(mapAreaName)

        if (mapAreaFile!!.exists()) {
            val offlineTileProvider = OfflineTileProvider(SimpleRegisterReceiver(context), arrayOf(mapAreaFile))
            mapView.tileProvider = offlineTileProvider

            try {
                //TODO: Offload to parallel async task
                val tileSourceString = offlineTileProvider.archives[0].tileSources.iterator().next()
                val tileSource = FileBasedTileSource.getSource(tileSourceString)

                mapView.setTileSource(tileSource)
            } catch (e: Exception) {
                mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            }
        } else {
            Toast.makeText(requireContext(), "Could not find MapArea", Toast.LENGTH_LONG).show()
        }

        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)

        viewModel.mapArea.value?.let {
            mapView.controller.animateTo(it.boundingBox.centerWithDateLine)
            mapView.setScrollableAreaLimitLatitude(it.boundingBox.latNorth, it.boundingBox.latSouth, 500)
            mapView.setScrollableAreaLimitLongitude(it.boundingBox.lonWest, it.boundingBox.lonEast, 500)
        }

    }

}