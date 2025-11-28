package com.mdm.app.managers

import android.content.Context
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.os.Build

data class NetworkStats(
    val appName: String,
    val packageName: String,
    val bytesSent: Long,
    val bytesReceived: Long
)

object NetworkMonitor {
    
    fun getNetworkStats(context: Context): List<NetworkStats> {
        val stats = mutableListOf<NetworkStats>()
        val pm = context.packageManager
        
        try {
            val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(0)
            }
            
            packages.forEach { packageInfo ->
                val uid = packageInfo.applicationInfo.uid
                val appName = pm.getApplicationLabel(packageInfo.applicationInfo).toString()
                
                val bytesSent = TrafficStats.getUidTxBytes(uid)
                val bytesReceived = TrafficStats.getUidRxBytes(uid)
                
                if (bytesSent > 0 || bytesReceived > 0) {
                    stats.add(
                        NetworkStats(
                            appName = appName,
                            packageName = packageInfo.packageName,
                            bytesSent = bytesSent,
                            bytesReceived = bytesReceived
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return stats.sortedByDescending { it.bytesSent + it.bytesReceived }
    }
    
    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "%.2f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
