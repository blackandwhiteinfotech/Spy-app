package com.mdm.app.managers

import android.app.ApplicationExitInfo
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

data class AppInfo(
    val name: String,
    val packageName: String,
    val versionName: String,
    val isSystemApp: Boolean
)

object AppManager {
    
    fun getInstalledApps(context: Context): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        val pm = context.packageManager
        
        try {
            val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(0)
            }
            
            packages.forEach { packageInfo ->
                val appInfo = packageInfo.applicationInfo
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                
                apps.add(
                    AppInfo(
                        name = pm.getApplicationLabel(appInfo).toString(),
                        packageName = packageInfo.packageName,
                        versionName = packageInfo.versionName ?: "Unknown",
                        isSystemApp = isSystemApp
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return apps.sortedBy { it.name }
    }
    
    fun getInstalledUserApps(context: Context): List<AppInfo> {
        return getInstalledApps(context).filter { !it.isSystemApp }
    }
    
    fun uninstallApp(context: Context, packageName: String): Boolean {
        return try {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_DELETE,
                android.net.Uri.parse("package:$packageName")
            )
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun installAppFromFile(context: Context, filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) return false
            
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW
            ).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
