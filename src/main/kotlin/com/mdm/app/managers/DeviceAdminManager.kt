package com.mdm.app.managers

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.mdm.app.receivers.MDMDeviceAdminReceiver

object DeviceAdminManager {
    
    fun isDeviceAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, MDMDeviceAdminReceiver::class.java)
        return dpm.isAdminActive(adminComponent)
    }
    
    fun enableDeviceAdmin(context: Context): Boolean {
        return isDeviceAdminActive(context)
    }
    
    fun lockDevice(context: Context): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(context, MDMDeviceAdminReceiver::class.java)
            
            if (dpm.isAdminActive(adminComponent)) {
                dpm.lockNow()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun factoryReset(context: Context): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(context, MDMDeviceAdminReceiver::class.java)
            
            if (dpm.isAdminActive(adminComponent)) {
                // Request confirmation before wiping
                dpm.wipeData(0)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun resetPassword(context: Context, password: String): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(context, MDMDeviceAdminReceiver::class.java)
            
            if (dpm.isAdminActive(adminComponent)) {
                @Suppress("DEPRECATION")
                dpm.resetPassword(password, 0)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
