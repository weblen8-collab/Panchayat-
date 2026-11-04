package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PanchayatSaffron,
    onPrimary = Color.White,
    primaryContainer = PanchayatSaffron.copy(alpha = 0.2f),
    onPrimaryContainer = PanchayatSaffronLight,
    secondary = TwitterBlue,
    onSecondary = Color.White,
    secondaryContainer = TwitterBlue.copy(alpha = 0.2f),
    onSecondaryContainer = TwitterBlue,
    tertiary = LikePink,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PanchayatSaffron,
    onPrimary = Color.White,
    primaryContainer = PanchayatSaffron.copy(alpha = 0.15f),
    onPrimaryContainer = PanchayatSaffron,
    secondary = TwitterBlue,
    onSecondary = Color.White,
    secondaryContainer = TwitterBlue.copy(alpha = 0.15f),
    onSecondaryContainer = TwitterBlue,
    tertiary = LikePink,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek Twitter/X dark mode
  dynamicColor: Boolean = false, // Keep high contrast branded identity
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

