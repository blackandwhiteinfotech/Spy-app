package com.mdm.app.services

import android.app.Service
import android.content.Intent
import android.content.Context
import android.location.LocationManager
import android.location.Location
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.mdm.app.R
import com.mdm.app.api.APIClient
import com.mdm.app.managers.PermissionManager

class LocationTrackingService : Service() {
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val handler = Handler(Looper.getMainLooper())
    private var updateInterval = 30000L // 30 seconds
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        startLocationUpdates()
        return START_STICKY
    }
    
    private fun startForeground() {
        val notification = NotificationCompat.Builder(this, "location_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.location_service))
            .setContentText(getString(R.string.location_active))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        ServiceCompat.startForeground(
            this,
            1,
            notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
    }
    
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    sendLocationUpdate(location)
                }
            }
        }
    }
    
    private fun startLocationUpdates() {
        if (!PermissionManager.hasPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            stopSelf()
            return
        }
        
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, updateInterval)
            .setMinUpdateIntervalMillis(updateInterval)
            .setMaxUpdateDelayMillis(updateInterval * 2)
            .build()
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            stopSelf()
        }
    }
    
    private fun sendLocationUpdate(location: Location) {
        handler.post {
            APIClient.sendLocationUpdate(
                location.latitude,
                location.longitude,
                location.accuracy
            )
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
