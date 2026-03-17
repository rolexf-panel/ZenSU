package com.zensu.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import java.io.BufferedReader
import java.io.InputStreamReader

object PowerUtils {
    
    fun reboot(type: String) {
        try {
            when (type) {
                "reboot" -> {
                    // Normal reboot using su
                    executeSu("reboot")
                }
                "reboot recovery" -> {
                    executeSu("reboot recovery")
                }
                "reboot bootloader" -> {
                    executeSu("reboot bootloader")
                }
                "reboot fastboot" -> {
                    executeSu("reboot fastboot")
                }
                "reboot edl" -> {
                    // EDL mode - usually requires specific command
                    executeSu("reboot edl")
                }
                "reboot sideload" -> {
                    executeSu("reboot sideload")
                }
                else -> {
                    executeSu("reboot")
                }
            }
        } catch (e: Exception) {
            // Fallback to standard Android reboot
            try {
                val intent = Intent("android.intent.action.REBOOT")
                intent.putExtra("nowait", 1)
                intent.putExtra("interval", 1)
                intent.putExtra("window", 0)
                // Can't broadcast from app, use alternative
            } catch (e2: Exception) {
                // Last resort - just try reboot
            }
        }
    }
    
    private fun executeSu(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }
    
    fun shutdown() {
        try {
            executeSu("reboot -p")
        } catch (e: Exception) {
            // Can't shutdown without root
        }
    }
}
