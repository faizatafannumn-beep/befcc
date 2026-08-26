package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private val BefccColorScheme = darkColorScheme(
    primary = BefccEmeraldPrimary,
    onPrimary = BefccBackgroundDark,
    primaryContainer = BefccEmeraldDark,
    onPrimaryContainer = BefccEmeraldLight,
    secondary = BefccGoldAccent,
    onSecondary = BefccBackgroundDark,
    secondaryContainer = BefccSurfaceCardElevated,
    onSecondaryContainer = BefccGoldLight,
    tertiary = BefccCrimsonAccent,
    onTertiary = BefccTextPrimary,
    background = BefccBackgroundDark,
    onBackground = BefccTextPrimary,
    surface = BefccSurfaceDark,
    onSurface = BefccTextPrimary,
    surfaceVariant = BefccSurfaceCard,
    onSurfaceVariant = BefccTextSecondary,
    outline = BefccBorderDark
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val activity = view.context.findActivity()
                if (activity != null) {
                    val window = activity.window
                    window.statusBarColor = BefccBackgroundDark.toArgb()
                    window.navigationBarColor = BefccBackgroundDark.toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    MaterialTheme(
        colorScheme = BefccColorScheme,
        typography = Typography,
        content = content
    )
}
