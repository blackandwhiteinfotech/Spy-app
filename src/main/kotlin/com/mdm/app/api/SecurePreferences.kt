package com.mdm.app.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePreferences {
    
    private lateinit var sharedPreferences: SharedPreferences
    private const val PREFS_NAME = "mdm_secure_prefs"
    
    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    
    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
    
    companion object {
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_SERVER_URL = "server_url"
        const val KEY_ENROLLMENT_CODE = "enrollment_code"
        const val KEY_IS_ENROLLED = "is_enrolled"
        const val KEY_LOCATION_ENABLED = "location_enabled"
        const val KEY_SCREEN_CAPTURE_ENABLED = "screen_capture_enabled"
        const val KEY_FILE_MANAGER_ENABLED = "file_manager_enabled"
        const val KEY_APP_MANAGEMENT_ENABLED = "app_management_enabled"
        const val KEY_DEVICE_ADMIN_ENABLED = "device_admin_enabled"
        const val KEY_PERMISSIONS_ACCEPTED = "permissions_accepted"
    }
}
