package com.zensu.model

enum class RootType {
    KERNELSU,
    KERNELSU_NEXT,
    MAGISK,
    APATCH,
    NONE
}

data class RootState(
    val isRooted: Boolean = false,
    val rootType: RootType = RootType.NONE,
    val version: String = "",
    val kmi: String = "",
    val isWorking: Boolean = false
)

data class Module(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val enabled: Boolean,
    val hasUpdate: Boolean = false,
    val path: String = "",
    val hasWebUI: Boolean = false,
    val webUIUrl: String = ""
)

data class AppProfile(
    val packageName: String,
    val appName: String,
    val uid: Int,
    val allow: Boolean = false,
    val gravity: Int = 0
)

data class Repo(
    val name: String,
    val url: String,
    val enabled: Boolean = true
)
