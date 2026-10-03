package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppStoreColorScheme = darkColorScheme(
  primary = PrimaryBlue,
  onPrimary = TextPrimary,
  primaryContainer = CardElevated,
  onPrimaryContainer = TextPrimary,
  secondary = AccentPurple,
  onSecondary = TextPrimary,
  background = BackgroundDark,
  onBackground = TextPrimary,
  surface = SurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = CardDark,
  onSurfaceVariant = TextSecondary,
  outline = CardBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = AppStoreColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        // `statusBarColor` / `navigationBarColor` are deprecated no-ops on
        // API 35+ and were doing nothing. Edge-to-edge is enabled, so the app
        // draws under the bars and content handles its own insets — all that's
        // left is making the bar icons light against the dark background.
        WindowCompat.getInsetsController(window, view).apply {
          isAppearanceLightStatusBars = false
          isAppearanceLightNavigationBars = false
        }
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
