package com.mdm.app.ui

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mdm.app.R
import com.mdm.app.api.SecurePreferences
import com.mdm.app.managers.AppManager
import com.mdm.app.managers.DeviceAdminManager
import com.mdm.app.managers.NetworkMonitor
import kotlinx.coroutines.*

class DashboardActivity : AppCompatActivity() {
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val SCREEN_CAPTURE_REQUEST = 200
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create UI dynamically
        val scrollView = ScrollView(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        // Title
        val titleView = TextView(this).apply {
            text = getString(R.string.dashboard_title)
            textSize = 24f
            setTextColor(resources.getColor(R.color.text_primary, theme))
            setPadding(0, 0, 0, 32)
        }
        mainLayout.addView(titleView)
        
        // Location Services Button
        val locationButton = createActionButton("Location Tracking") {
            toggleLocationTracking()
        }
        mainLayout.addView(locationButton)
        
        // File Manager Button
        val fileButton = createActionButton("File Manager") {
            showFileManager()
        }
        mainLayout.addView(fileButton)
        
        // App Management Button
        val appButton = createActionButton("App Manager") {
            showAppManager()
        }
        mainLayout.addView(appButton)
        
        // Network Monitor Button
        val networkButton = createActionButton("Network Monitor") {
            showNetworkMonitor()
        }
        mainLayout.addView(networkButton)
        
        // Screen Capture Button
        val screenButton = createActionButton("Screen Capture") {
            requestScreenCapture()
        }
        mainLayout.addView(screenButton)
        
        // Device Security Button
        val securityButton = createActionButton("Device Security") {
            showDeviceSecurity()
        }
        mainLayout.addView(securityButton)
        
        // Unenroll Button
        val unenrollButton = createActionButton("Unenroll Device") {
            showUnenrollDialog()
        }
        mainLayout.addView(unenrollButton)
        
        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
    
    private fun createActionButton(label: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            setOnClickListener { onClick() }
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }
    }
    
    private fun toggleLocationTracking() {
        val isEnabled = SecurePreferences.getBoolean(SecurePreferences.KEY_LOCATION_ENABLED, false)
        
        if (isEnabled) {
            stopService(Intent(this, com.mdm.app.services.LocationTrackingService::class.java))
            SecurePreferences.saveBoolean(SecurePreferences.KEY_LOCATION_ENABLED, false)
            Toast.makeText(this, "Location tracking stopped", Toast.LENGTH_SHORT).show()
        } else {
            startService(Intent(this, com.mdm.app.services.LocationTrackingService::class.java))
            SecurePreferences.saveBoolean(SecurePreferences.KEY_LOCATION_ENABLED, true)
            Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showFileManager() {
        AlertDialog.Builder(this)
            .setTitle("File Manager")
            .setMessage("This feature allows remote file upload/download/delete via the management dashboard.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun showAppManager() {
        scope.launch {
            val apps = AppManager.getInstalledUserApps(this@DashboardActivity)
            val appNames = apps.map { it.name }.toTypedArray()
            
            AlertDialog.Builder(this@DashboardActivity)
                .setTitle("Installed Apps")
                .setItems(appNames) { _, which ->
                    val app = apps[which]
                    showAppActions(app)
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
    
    private fun showAppActions(app: com.mdm.app.managers.AppInfo) {
        AlertDialog.Builder(this)
            .setTitle(app.name)
            .setItems(arrayOf("Uninstall", "Block", "Cancel")) { _, which ->
                when (which) {
                    0 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Confirm Uninstall")
                            .setMessage("Are you sure you want to uninstall ${app.name}?")
                            .setPositiveButton(R.string.yes) { _, _ ->
                                AppManager.uninstallApp(this, app.packageName)
                            }
                            .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                    1 -> {
                        Toast.makeText(this, "App blocking: ${app.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }
    
    private fun showNetworkMonitor() {
        scope.launch {
            val stats = NetworkMonitor.getNetworkStats(this@DashboardActivity)
            val statsText = stats.joinToString("\n") {
                "${it.appName}\nSent: ${NetworkMonitor.formatBytes(it.bytesSent)} | Received: ${NetworkMonitor.formatBytes(it.bytesReceived)}\n"
            }
            
            AlertDialog.Builder(this@DashboardActivity)
                .setTitle("Network Usage")
                .setMessage(statsText.ifEmpty { "No network activity detected" })
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
    
    private fun requestScreenCapture() {
        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        if (mpm != null) {
            AlertDialog.Builder(this)
                .setTitle("Screen Capture")
                .setMessage("This will allow the device to be viewed remotely. You will be prompted to confirm.")
                .setPositiveButton("Request Permission") { _, _ ->
                    startActivityForResult(mpm.createScreenCaptureIntent(), SCREEN_CAPTURE_REQUEST)
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == SCREEN_CAPTURE_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(this, "Screen capture permission granted", Toast.LENGTH_SHORT).show()
            // In production, would initialize virtual display and ImageReader
        }
    }
    
    private fun showDeviceSecurity() {
        AlertDialog.Builder(this)
            .setTitle("Device Security")
            .setItems(arrayOf("Lock Device", "Factory Reset", "Cancel")) { _, which ->
                when (which) {
                    0 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Lock Device")
                            .setMessage("Are you sure you want to lock this device?")
                            .setPositiveButton(R.string.yes) { _, _ ->
                                if (DeviceAdminManager.lockDevice(this)) {
                                    Toast.makeText(this, "Device locked", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Device admin not active", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                    1 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Factory Reset")
                            .setMessage("WARNING: This will erase all data on the device. Are you absolutely sure?")
                            .setPositiveButton(R.string.yes) { _, _ ->
                                if (DeviceAdminManager.factoryReset(this)) {
                                    Toast.makeText(this, "Factory reset initiated", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Device admin not active", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }
            }
            .show()
    }
    
    private fun showUnenrollDialog() {
        AlertDialog.Builder(this)
            .setTitle("Unenroll Device")
            .setMessage("Are you sure you want to unenroll this device? You will lose remote management capabilities.")
            .setPositiveButton(R.string.yes) { _, _ ->
                SecurePreferences.clear()
                stopService(Intent(this, com.mdm.app.services.LocationTrackingService::class.java))
                stopService(Intent(this, com.mdm.app.services.CommandExecutionService::class.java))
                stopService(Intent(this, com.mdm.app.services.APIService::class.java))
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
