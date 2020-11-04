package com.example.sheeptracker.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.res.ResourcesCompat
import com.example.sheeptracker.R
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.database.entities.TripMapPoint
import com.example.sheeptracker.ui.dateTimeFormatter
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.lang.Exception

class TripMapView: MapView {

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {

    }

    private val defaultZoomLevel = 18.0

    private val gpsTrail = Polyline()
    private val gpsStartMarker = Marker(this)
    private val gpsEndMarker = Marker(this)

    private val observationMarkers = ArrayList<Marker>()
    private val observationPolylines = ArrayList<Polyline>()

    /**
     * Sets ut the TripMapView to not have any UI interaction.
     */
    @SuppressLint("ClickableViewAccessibility")
    fun setupStaticOfflineView(mapAreaName: String) {
        setOnTouchListener { v, event ->
            true
        }
        setupOfflineView(mapAreaName)
    }

    private fun setupOfflineView(mapAreaName: String) {
        setUseDataConnection(false)
        val mapAreaFile = context?.getDatabasePath(mapAreaName)
        if (mapAreaFile!!.exists()) {
            val offlineTileProvider = OfflineTileProvider(SimpleRegisterReceiver(context), arrayOf(mapAreaFile))
            tileProvider = offlineTileProvider

            try {
                //TODO: Offload to parallel async task
                val tileSourceString = offlineTileProvider.archives[0].tileSources.iterator().next()
                val tileSource = FileBasedTileSource.getSource(tileSourceString)

                setTileSource(tileSource)
            } catch (e: Exception) {
                setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            }
        }

        invalidate()
    }

    /**
     * Zooms to show all GeoPoints.
     * Returns false if not able, else true.
     */
    fun zoomToGeoPoints(geoPoints: List<GeoPoint>): Boolean {
        if(geoPoints.isEmpty()) return false
        val geoPointsBoundingBox = BoundingBox.fromGeoPointsSafe(geoPoints)
        if (width == 0) {
            // Needs to wait for view to be inflated, hence using post{}
            post { zoomToBoundingBox(geoPointsBoundingBox, false, 50) }
        } else {
            zoomToBoundingBox(geoPointsBoundingBox, false, 50)
        }
        return true
    }

    fun zoomOutAndCenter(mapArea: MapArea) {
        mapArea.let {
            controller.setZoom(it.mapAreaMinZoom)
            controller.setCenter(it.boundingBox.centerWithDateLine)
        }
    }

    /**
     * Draws the GPS trail of a trip from given List<GeoPoint> with start and stop marker.
     * Does not add marker title or snippet.
     */
    fun drawSimpleGPSTrail(geoPoints: List<GeoPoint>) {
        gpsTrail.setPoints(geoPoints)
        if(!overlayManager.contains(gpsTrail)) {
            gpsTrail.outlinePaint.alpha = 200
            gpsTrail.outlinePaint.color = Color.parseColor("#404040")
            gpsTrail.outlinePaint.strokeWidth = 10.0f
            overlayManager.add(gpsTrail)
        }
        if(geoPoints.isNotEmpty() && !overlayManager.contains(gpsStartMarker)) {
            gpsStartMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            gpsStartMarker.position = geoPoints.first()
            gpsStartMarker.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_trip_origin_24, null)
            overlayManager.add(gpsStartMarker)
        }
        if(geoPoints.size > 1 && !overlayManager.contains(gpsEndMarker)) {
            gpsEndMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            gpsEndMarker.position = geoPoints.last()
            gpsEndMarker.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_stop_circle_24, null)
            overlayManager.add(gpsEndMarker)
        }
    }

    /**
     * Draws observation markers and lines to observation point.
     */
    fun drawSimpleObservationLinesAndMarkers(observations: List<Observation>, tripMapPoints: List<TripMapPoint>) {

        val observationsGeoPoints = observations.map { observation ->
            GeoPoint(observation.observationLat, observation.observationLon)
        }

        val observationTripMapGeoPoints = observations.map { observation ->
            val tripMapPoint = tripMapPoints.first { tripMapPoint ->
                tripMapPoint.tripMapPointId == observation.observationOwnerTripMapPointId
            }
            GeoPoint(tripMapPoint.tripMapPointLat, tripMapPoint.tripMapPointLon)
        }

        // Observation Markers
        observationsGeoPoints.forEachIndexed { i, geoPoint ->
            val marker = Marker(this)

            marker.position = geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

            marker.icon = observations[i].observationType.getDrawable(resources)

            observationMarkers.add(marker)
        }

        // Observation-TripMapPoint lines
        for (i in observationTripMapGeoPoints.indices) {
            val line = Polyline()
            line.setPoints(listOf(observationTripMapGeoPoints[i], observationsGeoPoints[i]))
            line.outlinePaint.color = Color.parseColor("#3399ff")
            line.outlinePaint.strokeWidth = 6.0f
            observationPolylines.add(line)

            observations[i].observationSecondaryTripMapPointId?.let {
                val x = tripMapPoints.first{ p -> p.tripMapPointId == it}
                x.let { p ->
                    val line = Polyline()
                    line.setPoints(listOf(
                        GeoPoint(p.tripMapPointLat, p.tripMapPointLon),
                        observationsGeoPoints[i]
                    ))
                    line.outlinePaint.color = Color.parseColor("#3399ff")
                    line.outlinePaint.strokeWidth = 6.0f
                    observationPolylines.add(line)
                }
            }
        }

        if(!overlayManager.containsAll(observationMarkers)) {
            overlayManager.removeAll(observationMarkers)
            overlayManager.addAll(observationMarkers)
        }

        if(!overlayManager.containsAll(observationPolylines)) {
            overlayManager.removeAll(observationPolylines)
            overlayManager.addAll(observationPolylines)
        }

    }

    fun drawFullGPSTrail(tripMapPoints: List<TripMapPoint>){
        val geoPoints = tripMapPoints.map { tripMapPoint -> GeoPoint(tripMapPoint.tripMapPointLat, tripMapPoint.tripMapPointLon) }
        drawSimpleGPSTrail(geoPoints)

        if(geoPoints.isNotEmpty()) {
            gpsStartMarker.title = dateTimeFormatter.format(tripMapPoints.first().tripMapPointDate)
            gpsStartMarker.snippet = this.context.getString(R.string.start)
        }

        if(geoPoints.size > 1) {
            gpsEndMarker.title = dateTimeFormatter.format(tripMapPoints.last().tripMapPointDate)
            gpsEndMarker.snippet = this.context.getString(R.string.end)
        }

    }

}