package com.mdm.app.managers

import android.content.Context
import android.os.Environment
import java.io.File

object FileManager {
    
    fun getPublicDocumentsDirectory(): File? {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    }
    
    fun getDeviceFileList(path: String = Environment.getExternalStorageDirectory().absolutePath): List<FileInfo> {
        val files = mutableListOf<FileInfo>()
        
        try {
            val directory = File(path)
            if (!directory.exists()) return files
            
            val listFiles = directory.listFiles() ?: return files
            
            listFiles.forEach { file ->
                files.add(
                    FileInfo(
                        name = file.name,
                        path = file.absolutePath,
                        size = file.length(),
                        isDirectory = file.isDirectory,
                        lastModified = file.lastModified()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return files.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
    }
    
    fun deleteFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun downloadFile(remotePath: String, localPath: String): Boolean {
        return try {
            val sourceFile = File(remotePath)
            val destFile = File(localPath)
            destFile.parentFile?.mkdirs()
            sourceFile.copyTo(destFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun uploadFile(localPath: String, remotePath: String): Boolean {
        return try {
            val sourceFile = File(localPath)
            val destFile = File(remotePath)
            destFile.parentFile?.mkdirs()
            sourceFile.copyTo(destFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "%.2f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}

data class FileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val lastModified: Long
)
