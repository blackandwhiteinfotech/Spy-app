package com.mdm.app.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.mdm.app.R
import com.mdm.app.api.SecurePreferences
import com.mdm.app.managers.PermissionManager

class PermissionsActivity : AppCompatActivity() {
    
    private val PERMISSION_REQUEST_CODE = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)
        
        setupUI()
    }
    
    private fun setupUI() {
        val permissionsList = findViewById<LinearLayout>(R.id.permissions_list)
        val acceptButton = findViewById<Button>(R.id.accept_button)
        
        // Display all permissions with descriptions
        PermissionManager.REQUIRED_PERMISSIONS.forEach { permission ->
            val permissionView = createPermissionView(permission)
            permissionsList.addView(permissionView)
        }
        
        acceptButton.setOnClickListener {
            showConsentDialog()
        }
    }
    
    private fun createPermissionView(permission: String): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        val titleView = TextView(this).apply {
            text = permission.split(".").last()
            textSize = 16f
            setTextColor(resources.getColor(R.color.text_primary, theme))
        }
        
        val descView = TextView(this).apply {
            text = PermissionManager.getPermissionDescription(permission)
            textSize = 12f
            setTextColor(resources.getColor(R.color.text_secondary, theme))
            setPadding(0, 8, 0, 0)
        }
        
        layout.addView(titleView)
        layout.addView(descView)
        
        return layout
    }
    
    private fun showConsentDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permissions_title)
            .setMessage("By accepting, you understand this app will:\n\n" +
                    "• Track your device location in real-time\n" +
                    "• Access and manage your files remotely\n" +
                    "• View your device screen\n" +
                    "• Manage installed applications\n" +
                    "• Monitor network usage\n" +
                    "• Lock/wipe device for security\n\n" +
                    "All actions require explicit consent. You can revoke permissions at any time.")
            .setPositiveButton(R.string.yes) { _, _ ->
                requestPermissions()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun requestPermissions() {
        val missingPermissions = PermissionManager.getMissingPermissions(this)
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions,
                PERMISSION_REQUEST_CODE
            )
        } else {
            proceedToEnrollment()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (PermissionManager.hasAllPermissions(this)) {
                SecurePreferences.saveBoolean(
                    SecurePreferences.KEY_PERMISSIONS_ACCEPTED,
                    true
                )
                proceedToEnrollment()
            }
        }
    }
    
    private fun proceedToEnrollment() {
        startActivity(Intent(this, EnrollmentActivity::class.java))
        finish()
    }
}
