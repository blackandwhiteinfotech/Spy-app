package com.mdm.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.mdm.app.R
import com.mdm.app.api.APIClient
import com.mdm.app.api.SecurePreferences
import kotlinx.coroutines.*

class EnrollmentActivity : AppCompatActivity() {
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enrollment)
        
        val enrollmentCodeInput = findViewById<EditText>(R.id.enrollment_code)
        val serverUrlInput = findViewById<EditText>(R.id.server_url)
        val enrollButton = findViewById<Button>(R.id.enroll_button)
        val progressBar = findViewById<ProgressBar>(R.id.enrollment_progress)
        
        enrollButton.setOnClickListener {
            val code = enrollmentCodeInput.text.toString().trim()
            val serverUrl = serverUrlInput.text.toString().trim()
            
            if (code.isEmpty() || serverUrl.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            progressBar.visibility = android.view.View.VISIBLE
            enrollButton.isEnabled = false
            
            performEnrollment(code, serverUrl)
        }
    }
    
    private fun performEnrollment(code: String, serverUrl: String) {
        scope.launch {
            try {
                val (success, token) = APIClient.enrollDevice(code)
                
                if (success && token.isNotEmpty()) {
                    // Save credentials
                    SecurePreferences.saveString(SecurePreferences.KEY_AUTH_TOKEN, token)
                    SecurePreferences.saveString(SecurePreferences.KEY_SERVER_URL, serverUrl)
                    SecurePreferences.saveString(SecurePreferences.KEY_ENROLLMENT_CODE, code)
                    SecurePreferences.saveBoolean(SecurePreferences.KEY_IS_ENROLLED, true)
                    
                    // Initialize API client
                    APIClient.init(this@EnrollmentActivity, serverUrl, token)
                    
                    // Start services
                    startService(Intent(this@EnrollmentActivity, com.mdm.app.services.LocationTrackingService::class.java))
                    startService(Intent(this@EnrollmentActivity, com.mdm.app.services.CommandExecutionService::class.java))
                    startService(Intent(this@EnrollmentActivity, com.mdm.app.services.APIService::class.java))
                    
                    showSuccessDialog()
                } else {
                    Toast.makeText(
                        this@EnrollmentActivity,
                        "Enrollment failed: $token",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@EnrollmentActivity,
                    "Enrollment error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enrollment Successful")
            .setMessage("Your device has been enrolled successfully. You can now manage it remotely.")
            .setPositiveButton("OK") { _, _ ->
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
