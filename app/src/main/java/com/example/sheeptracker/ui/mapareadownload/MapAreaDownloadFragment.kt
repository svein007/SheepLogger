package com.example.sheeptracker.ui.mapareadownload

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.MapAreaDownloadFragmentBinding
import com.example.sheeptracker.map.MapAreaManager

import org.osmdroid.config.Configuration
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.floor

class MapAreaDownloadFragment : Fragment() {

    private lateinit var viewModel: MapAreaDownloadViewModel
    private lateinit var binding: MapAreaDownloadFragmentBinding

    private var cacheManager: CacheManager? = null

    private var locationManager: LocationManager? = null
    private var locationOverlay: MyLocationNewOverlay? = null

    private val permissionRequestCode = 12

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.map_area_download_fragment, container, false
        )

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = MapAreaDownloadViewModelFactory(application, appDao)

        viewModel = ViewModelProvider(
            requireActivity(), viewModelFactory
        )[MapAreaDownloadViewModel::class.java]

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID  // Required to do API calls to OSM servers

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        locationManager = this.context?.applicationContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), binding.mapView)
        locationOverlay?.enableMyLocation()

        val tileSource = MapAreaManager.getOnlineTileSource()

        val position = GeoPoint(65.0, 14.0)

        binding.mapView.setTileSource(tileSource)

        binding.mapView.setMultiTouchControls(true)

        binding.mapView.minZoomLevel = 5.0
        binding.mapView.maxZoomLevel = 20.0

        binding.mapView.isTilesScaledToDpi = true
        binding.mapView.isFlingEnabled = true

        binding.mapView.setScrollableAreaLimitLatitude(72.0, 55.0, 0)
        binding.mapView.setScrollableAreaLimitLongitude(-2.0, 33.0, 0)

        binding.mapView.controller.setZoom(15.0)
        binding.mapView.controller.setCenter(position)

        //textView.text = "Zoom = %.2f".format(binding.mapView.zoomLevelDouble)

        binding.mapView.addMapListener(DelayedMapListener(object : MapListener {
            override fun onZoom(event: ZoomEvent?): Boolean {
                //textView.text = "Zoom = %.2f".format(binding.mapView.zoomLevelDouble)
                return false
            }

            override fun onScroll(event: ScrollEvent?): Boolean {
                return false
            }
        }))

        val marker = Marker(binding.mapView)
        marker.position = position
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        binding.mapView.overlays.add(marker)

        MapAreaManager.getLastKnownLocation(requireContext(), requireActivity(), permissionRequestCode)?.let {
            binding.mapView.controller.animateTo(it)
        }

        binding.mapView.overlays.add(locationOverlay)

        cacheManager = CacheManager(binding.mapView)

        val dm = requireContext().resources.displayMetrics
        val scaleBarOverlay = ScaleBarOverlay(binding.mapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        binding.mapView.overlays.add(scaleBarOverlay)

        binding.floatingActionButton.setOnClickListener {
            val possibleNumOfTiles = cacheManager?.possibleTilesInArea(
                binding.mapView.boundingBox,
                binding.mapView.zoomLevelDouble.toInt(), 20
            ) ?: 0

            viewModel.apply {
                tileCount = possibleNumOfTiles
                minZoom = floor(binding.mapView.zoomLevelDouble)
                maxZoom = binding.mapView.maxZoomLevel
                boundingBox = binding.mapView.boundingBox
            }

            findNavController().navigate(
                MapAreaDownloadFragmentDirections.actionMapAreaDownloadFragmentToNameMapAreaDialog()
            )
        }
    }


    override fun onResume() {
        super.onResume()

        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!requireActivity().isChangingConfigurations) {
            requireActivity().viewModelStore.clear()  // DANGEROUS?? maybe check if isConfigChange?
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MapAreaManager.getLastKnownLocation(requireContext(), requireActivity(), permissionRequestCode, false)?.let {
                    binding.mapView.controller.animateTo(it)
                }
            }
        }
    }

}