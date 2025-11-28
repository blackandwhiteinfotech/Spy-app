package com.mdm.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mdm.app.services.LocationTrackingService
import com.mdm.app.services.CommandExecutionService
import com.mdm.app.services.APIService

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start services on boot
            context.startService(Intent(context, LocationTrackingService::class.java))
            context.startService(Intent(context, CommandExecutionService::class.java))
            context.startService(Intent(context, APIService::class.java))
        }
    }
}
