package com.mdm.app.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext

data class APIResponse(
    val status: String,
    val message: String,
    val data: Map<String, Any>? = null
)

data class CommandRequest(
    val commandType: String,
    val parameters: Map<String, Any> = emptyMap()
)

object APIClient {
    
    private lateinit var httpClient: OkHttpClient
    private val gson = Gson()
    private var serverUrl: String = ""
    private var authToken: String = ""
    
    fun init(context: Context, url: String, token: String) {
        serverUrl = url.trimEnd('/')
        authToken = token
        
        // Create SSL context for HTTPS
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, null, null)
        
        httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory)
            .build()
    }
    
    fun sendLocationUpdate(latitude: Double, longitude: Double, accuracy: Float): Boolean {
        return try {
            val payload = mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "accuracy" to accuracy,
                "timestamp" to System.currentTimeMillis()
            )
            
            val requestBody = gson.toJson(payload)
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$serverUrl/api/device/location")
                .addHeader("Authorization", "Bearer $authToken")
                .post(requestBody)
                .build()
            
            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun fetchCommands(): List<CommandRequest> {
        return try {
            val request = Request.Builder()
                .url("$serverUrl/api/device/commands")
                .addHeader("Authorization", "Bearer $authToken")
                .get()
                .build()
            
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return emptyList()
                val apiResponse = gson.fromJson(body, APIResponse::class.java)
                
                // Convert API response to command requests
                @Suppress("UNCHECKED_CAST")
                val commands = apiResponse.data?.get("commands") as? List<Map<String, Any>>
                    ?: return emptyList()
                
                commands.map { cmdMap ->
                    CommandRequest(
                        commandType = cmdMap["type"] as? String ?: "",
                        parameters = (cmdMap["parameters"] as? Map<String, Any>) ?: emptyMap()
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    fun sendCommandResult(commandId: String, result: Map<String, Any>): Boolean {
        return try {
            val payload = mapOf(
                "commandId" to commandId,
                "result" to result,
                "timestamp" to System.currentTimeMillis()
            )
            
            val requestBody = gson.toJson(payload)
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$serverUrl/api/device/command-result")
                .addHeader("Authorization", "Bearer $authToken")
                .post(requestBody)
                .build()
            
            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun enrollDevice(enrollmentCode: String): Pair<Boolean, String> {
        return try {
            val payload = mapOf(
                "enrollmentCode" to enrollmentCode,
                "deviceId" to android.provider.Settings.Secure.getString(
                    null,
                    android.provider.Settings.Secure.ANDROID_ID
                )
            )
            
            val requestBody = gson.toJson(payload)
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$serverUrl/api/device/enroll")
                .post(requestBody)
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return Pair(false, "Empty response")
                val apiResponse = gson.fromJson(body, APIResponse::class.java)
                
                @Suppress("UNCHECKED_CAST")
                val token = (apiResponse.data?.get("token") as? String) ?: ""
                Pair(true, token)
            } else {
                Pair(false, "Enrollment failed: ${response.code}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Enrollment error: ${e.message}")
        }
    }
    
    fun sendDeviceInfo(info: Map<String, Any>): Boolean {
        return try {
            val requestBody = gson.toJson(info)
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$serverUrl/api/device/info")
                .addHeader("Authorization", "Bearer $authToken")
                .post(requestBody)
                .build()
            
            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
