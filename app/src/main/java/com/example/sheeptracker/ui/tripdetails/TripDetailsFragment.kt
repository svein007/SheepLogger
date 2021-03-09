package com.example.sheeptracker.ui.tripdetails

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.core.view.forEachIndexed
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.databinding.TripDetailsFragmentBinding
import com.example.sheeptracker.utils.copyFile
import com.example.sheeptracker.utils.getImageUrisForTrip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class TripDetailsFragment : Fragment() {

    lateinit var binding: TripDetailsFragmentBinding

    val viewModel: TripDetailsViewModel by viewModels {
        TripDetailsViewModelFactory(
            TripDetailsFragmentArgs.fromBundle(requireArguments()).tripId,
            requireNotNull(activity).application,
            AppDatabase.getInstance(requireNotNull(activity).application).appDatabaseDao
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.trip_details_fragment, container, false)

        setHasOptionsMenu(true)

        binding.mapOverlayClickView.setOnClickListener {
            findNavController().navigate(
                TripDetailsFragmentDirections.actionTripDetailsFragmentToTripFragment(
                    TripDetailsFragmentArgs.fromBundle(requireArguments()).tripId
                )
            )
        }

        binding.mapFAB.setOnClickListener {
            findNavController().navigate(
                TripDetailsFragmentDirections.actionTripDetailsFragmentToTripFragment(
                    TripDetailsFragmentArgs.fromBundle(requireArguments()).tripId
                )
            )
        }

        viewModel.mapArea.observe(viewLifecycleOwner) {
            it?.let { mapArea ->
                val mapAreaString = mapArea.getSqliteFilename()
                binding.tripDetailsMapView.setupStaticOfflineView(mapAreaString)
                binding.tripDetailsMapView.maxZoomLevel = it.mapAreaMaxZoom - 1
                viewModel.tripMapPoints.value?.let {
                    zoomToGeoPoints()
                }
            }
        }

        viewModel.tripMapPoints.observe(viewLifecycleOwner) {
            it?.let {
                val geoPoints = it.map { tripMapPoint -> GeoPoint(tripMapPoint.tripMapPointLat, tripMapPoint.tripMapPointLon) }
                binding.tripDetailsMapView.drawSimpleGPSTrail(geoPoints, true)
                zoomToGeoPoints()
                drawObservations()
            }
        }

        viewModel.observations.observe(viewLifecycleOwner) {
            it?.let {
                zoomToGeoPoints()
                drawObservations()
            }
        }

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onPause() {
        super.onPause()

        binding.tripDetailsMapView.onPause()
    }

    override fun onResume() {
        super.onResume()

        binding.tripDetailsMapView.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.trip_details_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.forEachIndexed { index, item ->
            when (item.itemId) {
                R.id.mi_export_trip_data -> {
                    viewModel.trip.value?.let {
                        item.setVisible(it.tripFinished)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_export_trip_data -> {
                exportTrip()
                return true
            }
        }
        return false
    }

    /** Helpers **/

    private fun zoomToGeoPoints() {
        val geoPoints = ArrayList<GeoPoint>()
        viewModel.tripMapPoints.value?.let {
            geoPoints.addAll(it.map { p -> GeoPoint(p.tripMapPointLat, p.tripMapPointLon) })
        }
        viewModel.observations.value?.let {
            geoPoints.addAll(it.map { o -> GeoPoint(o.observationLat, o.observationLon) })
        }
        binding.tripDetailsMapView.zoomToGeoPoints(geoPoints)
    }

    private fun drawObservations() {
        if (viewModel.observations.value != null && viewModel.tripMapPoints.value != null) {
            binding.tripDetailsMapView.drawSimpleObservationLinesAndMarkers(
                viewModel.observations.value!!,
                viewModel.tripMapPoints.value!!
            )
        }
    }

    private fun exportTrip() {
        val files = ArrayList<Uri>()

        CoroutineScope(Dispatchers.Main).launch {
            val exportIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                val dbFileCopy = copyFile(requireContext(), requireContext().getDatabasePath("sheep_database"), "db_trip", ".db")

                val db = dbFileCopy.path.let { SQLiteDatabase.openDatabase(it, null, SQLiteDatabase.OPEN_READWRITE) }

                db?.let {
                    Log.d("#####", it.toString())

                    viewModel.trip.value?.let { trip ->

                        val tripId = trip.tripId

                        it.delete("trip_table", "trip_id <> ?", arrayOf(tripId.toString()))
                        it.delete("trip_map_point_table", "trip_map_point_owner_trip_id <> ?", arrayOf(tripId.toString()))
                        it.delete("observation_table", "observation_owner_trip_id <> ?", arrayOf(tripId.toString()))

                        val mapAreaId = trip.tripOwnerMapAreaId
                        it.delete("map_area_table", "map_area_id <> ?", arrayOf(mapAreaId.toString()))

                        val observationIdCursor = it.rawQuery("SELECT observation_id FROM observation_table", null)

                        val observationIds = mutableListOf<Long>()
                        if (observationIdCursor.moveToFirst()){
                            do {
                                val id = observationIdCursor.getLong(0)
                                observationIds.add(id)
                            } while (observationIdCursor.moveToNext())
                        }

                        val sqlStatement = "DELETE FROM image_resource_table WHERE image_resource_observation_id NOT IN (${observationIds.joinToString(separator = ", ")})"
                        //Log.d("####", sqlStatement)
                        it.execSQL(sqlStatement)

                        it.execSQL("DELETE FROM animal_registration_table WHERE animal_registration_owner_observation_id NOT IN (${observationIds.joinToString(separator = ", ")})")
                        it.execSQL("DELETE FROM counter_table WHERE counter_owner_observation_id NOT IN (${observationIds.joinToString(separator = ", ")})")

                    }
                    it.close()
                }

                val dbFileExposed = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.sheeptracker.fileprovider",
                    dbFileCopy
                )

                files.add(dbFileExposed)

                val appDao =
                    AppDatabase.getInstance(requireContext()).appDatabaseDao

                getImageUrisForTrip(appDao, viewModel.trip.value?.tripId ?: -1).forEach {
                    val imgFile = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.sheeptracker.fileprovider",
                        it.toFile()
                    )
                    files.add(imgFile)
                }

                putExtra(Intent.EXTRA_SUBJECT, "Sheep Tracker Trip ${viewModel.trip.value?.tripId}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_TEXT, "Data for trip ${viewModel.trip.value?.tripId} is attached.")

                putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                type = "application/octet-stream"
            }
            startActivity(Intent.createChooser(exportIntent, "Export Trip Data"))
        }
    }

}