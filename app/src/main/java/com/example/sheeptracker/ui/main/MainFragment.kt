package com.example.sheeptracker.ui.main

import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.databinding.MainFragmentBinding
import com.example.sheeptracker.map.MapAreaManager
import kotlinx.android.synthetic.main.main_fragment.*

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

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding

    private var cacheManager: CacheManager? = null

    private var locationManager: LocationManager? = null
    private var locationOverlay: MyLocationNewOverlay? = null

    private val permissionRequestCode = 12

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.main_fragment, container, false
        )

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application

        val appDao = AppDatabase.getInstance(application).appDatabaseDao
        val viewModelFactory = MainViewModelFactory(application, appDao)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[MainViewModel::class.java]

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

        mapView.addMapListener(DelayedMapListener(object : MapListener {
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
    }


    override fun onResume() {
        super.onResume()

        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        binding.mapView.onPause()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.map_area_download_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_download_map_area) {
            showMapAreaNameDialog()
            return true
        }
        return false
    }


    private fun saveMapArea(mapAreaName: String) {
        val possibleNumOfTiles = cacheManager?.possibleTilesInArea(
            binding.mapView.boundingBox,
            binding.mapView.zoomLevelDouble.toInt(), 20
        ) ?: 0
        Toast.makeText(context, "#Tiles=" + possibleNumOfTiles, Toast.LENGTH_LONG).show()

        if (possibleNumOfTiles > 10000) {
            Toast.makeText(context, ">10000 tiles, use smaller area", Toast.LENGTH_LONG).show()
        } else if (binding.mapView.zoomLevelDouble.toInt() > 18 || possibleNumOfTiles < 40) {
            Toast.makeText(context, "Area too small, zoom out", Toast.LENGTH_LONG).show()
        } else {

            val mapArea = MapArea(
                mapAreaName = mapAreaName,
                mapAreaMinZoom = floor(binding.mapView.zoomLevelDouble),
                mapAreaMaxZoom = binding.mapView.maxZoomLevel,
                boundingBox = binding.mapView.boundingBox
            )

            viewModel.storeMapArea(mapArea)

            MapAreaManager.storeMapArea(
                requireContext(),
                binding.mapView,
                mapArea.getSqliteFilename()
            ) {
                findNavController().popBackStack()
            }
        }
    }

    private fun showMapAreaNameDialog() {
        val viewGroup = LinearLayout(requireContext())
        viewGroup.setPadding(48, 0, 48, 0)

        val editText = EditText(requireContext())
        editText.hint = getString(R.string.map_area_name)
        editText.inputType = editText.inputType or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        editText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT)

        viewGroup.addView(editText)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.download_mapArea))
            .setMessage(getString(R.string.enter_map_area_name))
            .setView(viewGroup)
            .setPositiveButton(getString(R.string.download)) { dialog, which ->
                val mapAreaName = editText.text.toString()
                saveMapArea(mapAreaName)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            }
            .show()

    }

}