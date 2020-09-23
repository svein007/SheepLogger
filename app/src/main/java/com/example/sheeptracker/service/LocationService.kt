package com.example.sheeptracker.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.database.entities.TripMapPoint
import com.example.sheeptracker.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.util.*

class LocationService : Service() {
    private val NOTIFICATION_CHANNEL_ID = "SheepTracker LocationService"

    private lateinit var locationManager: LocationManager

    private var tripId: Long? = null

    private val locationListener = object : LocationListener
    {
        private var lastLocation: Location? = null

        override fun onLocationChanged(location: Location?) {
            Log.d("#####", "LocationService is to log location: " + location.toString())
            location?.let {
                logLocationToTripTrail(it)
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }

        private fun logLocationToTripTrail(location: Location) {
            // Min 5 meters between adjacent points, TODO: use settings
            val minDistance = 5.0f

            if (lastLocation == null || location.distanceTo(lastLocation) > minDistance) {
                // Log new location
                CoroutineScope(Dispatchers.Main).launch {

                    val lastGeoPoint = if (lastLocation == null) {
                        val lastTripMapPoint = getTripMapPoints(tripId!!).maxByOrNull { tripMapPoint -> tripMapPoint.tripMapPointId }
                        if(lastTripMapPoint != null) {
                            GeoPoint(lastTripMapPoint!!.tripMapPointLat, lastTripMapPoint.tripMapPointLon)
                        } else {
                            null
                        }
                    } else {
                        GeoPoint(lastLocation!!.latitude, lastLocation!!.longitude)
                    }

                    val distance = lastGeoPoint?.distanceToAsDouble(GeoPoint(location.latitude, location.longitude)) ?: 100.0
                    if (distance > minDistance) {
                        tripId?.let {
                            val tripMapPoint = TripMapPoint(
                                tripMapPointLat =  location.latitude,
                                tripMapPointLon =  location.longitude,
                                tripMapPointDate = Date(),
                                tripMapPointOwnerTripId = it
                            )
                            insert(tripMapPoint)
                        }
                    } else {
                        Log.d("####", "Did not add GeoPoint, too close to last pinned GeoPoint. ${distance}m")
                    }
                }
            }
        }

        private suspend fun insert(point: TripMapPoint): Long {
            return withContext(Dispatchers.IO) {
                AppDatabase.getInstance(applicationContext).appDatabaseDao.insert(point)
            }
        }

        private suspend fun getTripMapPoints(tripId: Long): List<TripMapPoint> {
            return withContext(Dispatchers.IO) {
                AppDatabase.getInstance(applicationContext).appDatabaseDao.getTripMapPointsForTrip(tripId)
            }
        }

    }

    companion object {
        fun startService(context: Context, tripId: Long) {
            val startIntent = Intent(context, LocationService::class.java)
            startIntent.putExtra("tripId", tripId)
            ContextCompat.startForegroundService(context, startIntent)
        }
        fun stopService(context: Context) {
            val stopIntent = Intent(context, LocationService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 20.0f, locationListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //return super.onStartCommand(intent, flags, startId)

        //do heavy work on a background thread
        val tripIdFromIntent = intent?.getLongExtra("tripId", -1)
        tripId = tripIdFromIntent
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("SheepTracker")
            .setContentText("Tracking location for ongoing trip.")
            .setSmallIcon(R.drawable.ic_baseline_trip_origin_24)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        //stopSelf();
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "SheepTracker LocationService",
                NotificationManager.IMPORTANCE_DEFAULT)
            serviceChannel.setSound(null, null)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

}