package com.example.appfoco.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryFire,
    onPrimary = TextCream,
    secondary = SecondaryGold,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = TextCream,
    surface = DarkSurface,
    onSurface = TextCream,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextCream,
    error = RedDanger,
    onError = TextCream
)

@Composable
fun AppFocoTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalContext.current as? Activity
    if (view != null) {
        SideEffect {
            val window = view.window
            window.statusBarColor = Color(0xFF1A0903).toArgb()
            window.navigationBarColor = Color(0xFF1A0903).toArgb()
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
