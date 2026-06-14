package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.ui.viewmodel.RestaurantViewModel

object ThemeState {
    var currentPrimaryColor by mutableStateOf(Color(0xFFE65100))
}

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
  )

@Composable
fun MyApplicationTheme(
  viewModel: RestaurantViewModel? = null,
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val themeName = viewModel?.currentTheme?.collectAsState()?.value ?: "Orange"
  val textSizeName = viewModel?.currentTextSize?.collectAsState()?.value ?: "Medium"

  val primaryColor = when(themeName) {
    "Blue" -> Color(0xFF3F51B5)
    "Green" -> Color(0xFF2E7D32)
    "Gold" -> Color(0xFFFF8F00)
    else -> Color(0xFFE65100) // "Orange"
  }

  LaunchedEffect(themeName) {
    ThemeState.currentPrimaryColor = primaryColor
  }

  val textMultiplier = when(textSizeName) {
    "Small" -> 0.82f
    "Large" -> 1.18f
    else -> 1.0f // "Medium"
  }

  val currentDensity = LocalDensity.current
  val customDensity = remember(currentDensity, textMultiplier) {
    object : Density by currentDensity {
      override val fontScale: Float
        get() = currentDensity.fontScale * textMultiplier
    }
  }

  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> darkColorScheme(primary = primaryColor)
      else -> lightColorScheme(primary = primaryColor)
    }

  CompositionLocalProvider(LocalDensity provides customDensity) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
