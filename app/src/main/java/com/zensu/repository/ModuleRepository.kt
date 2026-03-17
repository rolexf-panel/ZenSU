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
            
            // List modules using su
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
            
            // Read module.prop
            val propContent = executeSu("cat $modulePath/module.prop 2>/dev/null")
            if (propContent.isEmpty()) return null
            
            val prop = Properties()
            prop.load(propContent.byteInputStream(Charsets.UTF_8))
            
            // Clean up description - remove newlines and special chars
            val rawDescription = prop.getProperty("description", "")
            val cleanDescription = rawDescription
                .replace("\n", " ")
                .replace("\r", "")
                .replace("\\s+".toRegex(), " ")
                .trim()
            
            // Check if enabled
            val disableCheck = executeSu("ls $modulePath/disable 2>/dev/null")
            val enabled = disableCheck.isEmpty()
            
            Module(
                id = dirName,
                name = prop.getProperty("name", dirName),
                version = prop.getProperty("version", "Unknown"),
                author = prop.getProperty("author", "Unknown"),
                description = cleanDescription,
                enabled = enabled,
                path = modulePath
            )
        } catch (e: Exception) {
            null
        }
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
    
    private fun executeSu(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()
            process.waitFor()
            output.trim()
        } catch (e: Exception) {
            ""
        }
    }
}
