package com.example.osmdroidexample.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.StrictMode
import androidx.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.osmdroidexample.R
import kotlinx.android.synthetic.main.main_fragment.*

import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.modules.GEMFFileArchive
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider
import org.osmdroid.tileprovider.modules.TileWriter
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.*
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.wms.WMSEndpoint
import org.osmdroid.wms.WMSParser
import org.osmdroid.wms.WMSTileSource
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Configuration.getInstance().load(context?.applicationContext, PreferenceManager.getDefaultSharedPreferences(context?.applicationContext))

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID  // Required to do API calls to OSM servers

        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel

        val tileSource = object : OnlineTileSourceBase("Kartverket - Norge Topografisk", 0,
                20, 256, "",
                //arrayOf("https://wms.geonorge.no/skwms1/wms.bakgrunnskart_havarealverktoey?service=WMS")) {
                //arrayOf("https://wms.geonorge.no/skwms1/wms.dybderelieffpolar_v02")) {

                // VIRKER... men må nok bruke WSM-url (cache er raskere men ikke like bra resolution)
                arrayOf("https://opencache.statkart.no/gatekeeper/gk/gk.open_gmaps")) {

            override fun getTileURLString(pMapTileIndex: Long): String {
                return (baseUrl + "?"
                        + "layers=topo4&"
                        + "zoom=" + MapTileIndex.getZoom(pMapTileIndex) + "&"
                        + "y=" + MapTileIndex.getY(pMapTileIndex) + "&"
                        + "x=" + MapTileIndex.getX(pMapTileIndex))
            }
        }

        //val tileWriter = TileWriter()
        //val fileSystemProvider = MapTileFilesystemProvider(registerReciever, tileSource)

        // GEMFFileArchive.getGEMFFileArchive(mGemfArchiveFilename)
        /*
        val wmstilesource = WMSTileSource("WMS Kartverket Norge Grunnkart",
                arrayOf("https://opencache.statkart.no/gatekeeper/gk/gk.open_gmaps?layers=norges_grunnkart&zoom={z}&x={x}&y={y}"),
                "landareal","1.0.0","GetMap","default",4)
        */
        //mapView.setTileSource(TileSourceFactory.MAPNIK)

        mapView.setTileSource(tileSource)
        mapView.setMultiTouchControls(true)

        mapView.minZoomLevel = 5.0
        mapView.maxZoomLevel = 20.0

        mapView.isTilesScaledToDpi = true
        mapView.isFlingEnabled = true

        mapView.controller.setZoom(5.0)
        mapView.controller.setCenter(GeoPoint(65.0, 14.0))

    }

    override fun onResume() {
        super.onResume()

        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        mapView.onPause()
    }

}