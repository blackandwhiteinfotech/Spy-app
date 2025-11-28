package com.mdm.app.managers

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {
    
    // List of required permissions
    val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.FOREGROUND_SERVICE,
        android.Manifest.permission.CAPTURE_VIDEO_OUTPUT
    )
    
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasAllPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { hasPermission(context, it) }
    }
    
    fun getMissingPermissions(context: Context): Array<String> {
        return REQUIRED_PERMISSIONS.filter { !hasPermission(context, it) }.toTypedArray()
    }
    
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            android.Manifest.permission.ACCESS_FINE_LOCATION -> "Fine location for GPS tracking"
            android.Manifest.permission.ACCESS_COARSE_LOCATION -> "Coarse location for network-based tracking"
            android.Manifest.permission.READ_EXTERNAL_STORAGE -> "Read files for remote file management"
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Write files for remote file management"
            android.Manifest.permission.INTERNET -> "Internet access for API communication"
            android.Manifest.permission.ACCESS_NETWORK_STATE -> "Monitor network usage"
            android.Manifest.permission.FOREGROUND_SERVICE -> "Background service for location tracking"
            android.Manifest.permission.CAPTURE_VIDEO_OUTPUT -> "Screen capture for remote viewing"
            else -> permission
        }
    }
}
