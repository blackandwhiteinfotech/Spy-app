package com.mdm.app

import android.app.Application
import android.content.Context
import com.mdm.app.api.SecurePreferences

class MDMApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize secure preferences
        SecurePreferences.init(this)
    }
    
    companion object {
        var context: Context? = null
            private set
        
        fun getAppContext(): Context? = context
    }
}
