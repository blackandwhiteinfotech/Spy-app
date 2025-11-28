package com.mdm.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import com.mdm.app.api.APIClient
import com.mdm.app.managers.NetworkMonitor
import kotlinx.coroutines.*

class APIService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private val syncInterval = 60000L // 1 minute
    
    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startSyncTimer()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startSyncTimer()
        }
        return START_STICKY
    }
    
    private fun startSyncTimer() {
        serviceScope.launch {
            while (isRunning) {
                try {
                    // Sync network usage statistics
                    val networkStats = NetworkMonitor.getNetworkStats(this@APIService)
                    val statsMap = networkStats.associate {
                        it.packageName to mapOf(
                            "name" to it.appName,
                            "sent" to it.bytesSent,
                            "received" to it.bytesReceived
                        )
                    }
                    
                    APIClient.sendDeviceInfo(
                        mapOf(
                            "type" to "network_stats",
                            "timestamp" to System.currentTimeMillis(),
                            "stats" to statsMap
                        )
                    )
                    
                    delay(syncInterval)
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(syncInterval)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
