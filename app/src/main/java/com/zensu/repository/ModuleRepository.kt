package com.zensu.repository

import com.zensu.model.Module
import com.zensu.model.RootType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Properties

object ModuleRepository {
    
    suspend fun getModules(rootType: RootType): List<Module> = withContext(Dispatchers.IO) {
        try {
            val modulesPath = when (rootType) {
                RootType.KERNELSU, RootType.KERNELSU_NEXT -> "/data/adb/modules"
                RootType.MAGISK -> "/data/adb/modules"
                RootType.APATCH -> "/data/adb/apatch/modules"
                RootType.NONE -> ""
            }
            
            if (modulesPath.isEmpty()) {
                return@withContext emptyList()
            }
            
            val moduleDirs = executeSu("ls $modulesPath").split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            
            moduleDirs.mapNotNull { dirName ->
                parseModule(modulesPath, dirName)
            }.sortedBy { it.name.lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseModule(modulesPath: String, dirName: String): Module? {
        return try {
            val modulePath = "$modulesPath/$dirName"
            
            // Read module.prop with UTF-8, fallback to ISO-8859-1
            var propContent = executeSu("cat $modulePath/module.prop 2>/dev/null")
            if (propContent.isEmpty()) return null
            
            // Try to fix encoding issues - replace unknown chars
            propContent = fixEncoding(propContent)
            
            val prop = Properties()
            try {
                prop.load(propContent.byteInputStream(Charsets.UTF_8))
            } catch (e: Exception) {
                try {
                    prop.load(propContent.byteInputStream(Charsets.ISO_8859_1))
                } catch (e2: Exception) {
                    // Fallback - use raw content
                }
            }
            
            // Clean description - handle encoding issues
            var rawDescription = prop.getProperty("description", "")
            rawDescription = fixEncoding(rawDescription)
            val cleanDescription = rawDescription
                .replace("\n", " ")
                .replace("\r", "")
                .replace("\t", " ")
                .replace(Regex("[^\\x20-\\x7E\\xA0-\\xFF]"), " ")  // Keep only printable chars
                .replace(Regex("\\s+"), " ")
                .trim()
            
            // Check if enabled
            val disableCheck = executeSu("ls $modulePath/disable 2>/dev/null")
            val enabled = disableCheck.isEmpty()
            
            // Check for WebUI - look for webui or post-fs-data.sh / service.sh
            val hasWebUI = executeSu("ls $modulePath/webui 2>/dev/null").isNotEmpty() ||
                          executeSu("ls $modulePath/post-fs-data.sh 2>/dev/null").isNotEmpty() ||
                          executeSu("ls $modulePath/service.sh 2>/dev/null").isNotEmpty()
            
            // Get WebUI URL if available
            val webUIUrl = if (hasWebUI) "http://127.0.0.1:${executeSu("cat $modulePath/webui_port 2>/dev/null").trim().ifEmpty { "5555" }}" else ""
            
            Module(
                id = dirName,
                name = fixEncoding(prop.getProperty("name", dirName)),
                version = prop.getProperty("version", "Unknown"),
                author = fixEncoding(prop.getProperty("author", "Unknown")),
                description = cleanDescription,
                enabled = enabled,
                path = modulePath,
                hasWebUI = hasWebUI,
                webUIUrl = webUIUrl
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun fixEncoding(text: String): String {
        // Replace common encoding issues with proper characters
        return text
            .replace("\uFFFD", "")  // Replacement character
            .replace("Ã©", "e")
            .replace("Ã¨", "e")
            .replace("Ã ", "a")
            .replace("Ã¢", "a")
            .replace("Ã®", "i")
            .replace("Ã´", "o")
            .replace("Ã»", "u")
            .replace("Ã§", "c")
            .replace("Ã‰", "E")
            .replace("â€™", "'")
            .replace("â€œ", "\"")
            .replace("â€", "\"")
            .replace("â€“", "-")
            .replace("â€¢", "•")
    }
    
    suspend fun toggleModule(module: Module, enable: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val modulePath = module.path
            
            val result = if (enable) {
                executeSu("rm -f $modulePath/disable")
            } else {
                executeSu("touch $modulePath/disable")
            }
            
            result.isNotEmpty() || true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun installModule(zipPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if zip contains module
            val check = executeSu("unzip -l '$zipPath' 2>/dev/null | grep module.prop")
            if (check.isEmpty()) {
                return@withContext false
            }
            
            // Extract to modules directory
            val modulesPath = "/data/adb/modules"
            val extractResult = executeSu("unzip -o '$zipPath' -d $modulesPath 2>/dev/null")
            
            extractResult.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun removeModule(module: Module): Boolean = withContext(Dispatchers.IO) {
        try {
            val modulePath = module.path
            val result = executeSu("rm -rf $modulePath")
            result.isNotEmpty() || true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun executeSu(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream, Charsets.UTF_8))
            val output = reader.readText()
            reader.close()
            process.waitFor()
            output.trim()
        } catch (e: Exception) {
            ""
        }
    }
}
