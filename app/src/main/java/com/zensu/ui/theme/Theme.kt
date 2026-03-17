package com.zensu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private val _darkMode = MutableStateFlow<Boolean?>(null)
    val darkMode: StateFlow<Boolean?> = _darkMode.asStateFlow()
    
    fun setDarkMode(useSystem: Boolean, manualValue: Boolean? = null) {
        _darkMode.value = if (useSystem) null else manualValue
    }
}

@Composable
fun ZenSUTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val manualDark = ThemeManager.darkMode.collectAsState()
    
    val darkTheme = manualDark.value ?: systemDark
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = ZenPrimaryDark,
            secondary = ZenSecondaryDark,
            tertiary = ZenTertiaryDark,
            background = ZenBackgroundDark,
            surface = ZenSurfaceDark,
            onPrimary = ZenOnPrimaryDark,
            onSecondary = ZenOnSecondaryDark,
            onBackground = ZenOnBackgroundDark,
            onSurface = ZenOnSurfaceDark
        )
        else -> lightColorScheme(
            primary = ZenPrimary,
            secondary = ZenSecondary,
            tertiary = ZenTertiary,
            background = ZenBackground,
            surface = ZenSurface,
            onPrimary = ZenOnPrimary,
            onSecondary = ZenOnSecondary,
            onBackground = ZenOnBackground,
            onSurface = ZenOnSurface
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
