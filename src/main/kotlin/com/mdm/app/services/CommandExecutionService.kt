package com.mdm.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import com.mdm.app.api.APIClient
import com.mdm.app.api.SecurePreferences
import kotlinx.coroutines.*

class CommandExecutionService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    
    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startCommandPolling()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startCommandPolling()
        }
        return START_STICKY
    }
    
    private fun startCommandPolling() {
        serviceScope.launch {
            while (isRunning) {
                try {
                    val commands = APIClient.fetchCommands()
                    
                    commands.forEach { command ->
                        handler.post {
                            executeCommand(command)
                        }
                    }
                    
                    delay(10000) // Poll every 10 seconds
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(10000)
                }
            }
        }
    }
    
    private fun executeCommand(command: com.mdm.app.api.CommandRequest) {
        when (command.commandType) {
            "LOCK_DEVICE" -> handleLockDevice(command)
            "FACTORY_RESET" -> handleFactoryReset(command)
            "GET_LOCATION" -> handleGetLocation(command)
            "GET_DEVICE_INFO" -> handleGetDeviceInfo(command)
            "GET_INSTALLED_APPS" -> handleGetInstalledApps(command)
            "FILE_OPERATION" -> handleFileOperation(command)
            else -> {}
        }
    }
    
    private fun handleLockDevice(command: com.mdm.app.api.CommandRequest) {
        val success = com.mdm.app.managers.DeviceAdminManager.lockDevice(this)
        reportCommandResult(
            command.parameters["commandId"] as? String ?: "",
            mapOf("success" to success, "action" to "lock_device")
        )
    }
    
    private fun handleFactoryReset(command: com.mdm.app.api.CommandRequest) {
        val success = com.mdm.app.managers.DeviceAdminManager.factoryReset(this)
        reportCommandResult(
            command.parameters["commandId"] as? String ?: "",
            mapOf("success" to success, "action" to "factory_reset")
        )
    }
    
    private fun handleGetLocation(command: com.mdm.app.api.CommandRequest) {
        // Location is handled by LocationTrackingService
        reportCommandResult(
            command.parameters["commandId"] as? String ?: "",
            mapOf("success" to true, "action" to "get_location")
        )
    }
    
    private fun handleGetDeviceInfo(command: com.mdm.app.api.CommandRequest) {
        val deviceInfo = mapOf(
            "device_name" to android.os.Build.DEVICE,
            "device_model" to android.os.Build.MODEL,
            "android_version" to android.os.Build.VERSION.RELEASE,
            "sdk_level" to android.os.Build.VERSION.SDK_INT,
            "manufacturer" to android.os.Build.MANUFACTURER
        )
        
        APIClient.sendDeviceInfo(deviceInfo)
        
        reportCommandResult(
            command.parameters["commandId"] as? String ?: "",
            mapOf("success" to true, "action" to "get_device_info", "data" to deviceInfo)
        )
    }
    
    private fun handleGetInstalledApps(command: com.mdm.app.api.CommandRequest) {
        val apps = com.mdm.app.managers.AppManager.getInstalledApps(this)
        val appList = apps.map {
            mapOf(
                "name" to it.name,
                "package" to it.packageName,
                "version" to it.versionName,
                "system" to it.isSystemApp
            )
        }
        
        reportCommandResult(
            command.parameters["commandId"] as? String ?: "",
            mapOf("success" to true, "action" to "get_installed_apps", "apps" to appList)
        )
    }
    
    private fun handleFileOperation(command: com.mdm.app.api.CommandRequest) {
        val operation = command.parameters["operation"] as? String ?: ""
        val filePath = command.parameters["path"] as? String ?: ""
        
        val success = when (operation) {
            "delete" -> com.mdm.app.managers.FileManager.deleteFile(filePath)
            "list" -> {
                com.mdm.app.managers.FileManager.getDeviceFileList(filePath)
                true
            }
            else -> false
        }
        
        reportCommandResult(
            command.parameters["commandId"] as? String ?: "",
            mapOf("success" to success, "action" to "file_operation", "operation" to operation)
        )
    }
    
    private fun reportCommandResult(commandId: String, result: Map<String, Any>) {
        APIClient.sendCommandResult(commandId, result)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
