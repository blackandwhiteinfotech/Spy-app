package com.mdm.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mdm.app.R
import com.mdm.app.managers.PermissionManager

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val enrollButton = findViewById<Button>(R.id.enroll_button)
        val dashboardButton = findViewById<Button>(R.id.dashboard_button)
        
        enrollButton.setOnClickListener {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }
        
        dashboardButton.setOnClickListener {
            if (PermissionManager.hasAllPermissions(this)) {
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this, PermissionsActivity::class.java))
            }
        }
    }
}
