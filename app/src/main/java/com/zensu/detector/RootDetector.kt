package com.zensu.detector

import com.zensu.model.RootState
import com.zensu.model.RootType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object RootDetector {
    
    suspend fun detectRoot(): RootState = withContext(Dispatchers.IO) {
        try {
            // Try to get root using root shell
            val rootDetected = executeWithRoot("ls /data/adb") || 
                              executeWithRoot("ls /data/adb/ksu") ||
                              executeWithRoot("ls /data/adb/ksun") ||
                              executeWithRoot("which magisk") ||
                              executeWithRoot("ls /data/adb/apatch")
            
            if (!rootDetected) {
                return@withContext RootState(
                    isRooted = false,
                    rootType = RootType.NONE,
                    version = "",
                    isWorking = false
                )
            }
            
            // Determine root type
            val rootType = when {
                executeWithRoot("ls /data/adb/ksu") -> RootType.KERNELSU
                executeWithRoot("ls /data/adb/ksun") -> RootType.KERNELSU_NEXT
                executeWithRoot("ls /data/adb/apatch") -> RootType.APATCH
                executeWithRoot("which magisk") || File("/sbin/magisk").exists() -> RootType.MAGISK
                else -> RootType.NONE
            }
            
            val version = getRootVersion(rootType)
            
            RootState(
                isRooted = true,
                rootType = rootType,
                version = version,
                isWorking = true
            )
        } catch (e: Exception) {
            RootState(
                isRooted = false,
                rootType = RootType.NONE,
                version = "",
                isWorking = false
            )
        }
    }
    
    private fun executeWithRoot(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()
            process.waitFor()
            process.exitValue() == 0 && output.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getRootVersion(rootType: RootType): String {
        // Try multiple methods to get version
        val commands = when (rootType) {
            RootType.KERNELSU, RootType.KERNELSU_NEXT -> listOf(
                "ksu version",
                "cat /data/adb/ksu/version 2>/dev/null",
                "cat /sys/module/kernelsu/version 2>/dev/null",
                "magisk -v"
            )
            RootType.MAGISK -> listOf(
                "magisk -v",
                "cat /sbin/.magisk/version 2>/dev/null"
            )
            RootType.APATCH -> listOf(
                "apatch version",
                "cat /data/adb/apatch/version 2>/dev/null"
            )
            RootType.NONE -> listOf("echo unknown")
        }
        
        for (command in commands) {
            val version = tryExecute(command)
            if (version.isNotEmpty() && version != "unknown") {
                return version.trim()
            }
        }
        
        return "Unknown"
    }
    
    private fun tryExecute(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = reader.readText().trim()
            val error = errorReader.readText()
            reader.close()
            errorReader.close()
            process.waitFor()
            
            if (process.exitValue() == 0 && output.isNotEmpty()) {
                output
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    fun requestRootAccess() {
        try {
            // This will trigger the root request dialog
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            process.waitFor()
        } catch (e: Exception) {
            // Ignore - the user will be shown the root required screen
        }
    }
}
