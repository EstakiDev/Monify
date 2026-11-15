package dev.estaki.myFinancialApp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.estaki.ui_utils.ui.theme.DarkGray
import dev.estaki.ui_utils.ui.theme.TypographyDark
import dev.estaki.ui_utils.ui.theme.TypographyLite
import dev.estaki.ui_utils.ui.theme.White

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFCA73),
    onPrimary = Color(0xFF402B00),
    primaryContainer = Color(0xFF594000),
    onPrimaryContainer = Color(0xFFFFDEAA),
    secondary = Color(0xFFAACAD8),
    onSecondary = Color(0xFF163541),
    secondaryContainer = Color(0xFF2E4B58),
    onSecondaryContainer = Color(0xFFC6E7F5),
    background = Color(0xFF101820),
    onBackground = Color(0xFFE1E5E8),
    surface = Color(0xFF101820),
    onSurface = Color(0xFFE1E5E8),
    surfaceVariant = Color(0xFF40484C),
    onSurfaceVariant = Color(0xFFC0C8CB),
    surfaceContainerLowest = Color(0xFF0B1219),
    surfaceContainerLow = Color(0xFF182128),
    surfaceContainer = Color(0xFF1C252C),
    surfaceContainerHigh = Color(0xFF263038),
    surfaceContainerHighest = Color(0xFF313B43),
    inverseSurface = DarkGray,
    inverseOnSurface = White

)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF805600),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDEAA),
    onPrimaryContainer = Color(0xFF291800),
    secondary = Color(0xFF46636F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC6E7F5),
    onSecondaryContainer = Color(0xFF001F29),
    background = Color(0xFFFAF9F6),
    onBackground = Color(0xFF1A2023),
    surface = Color(0xFFFAF9F6),
    onSurface = Color(0xFF1A2023),
    surfaceVariant = Color(0xFFDEE3E5),
    onSurfaceVariant = Color(0xFF40484C),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF3F4F2),
    surfaceContainer = Color(0xFFEDEEEC),
    surfaceContainerHigh = Color(0xFFE7E9E7),
    surfaceContainerHighest = Color(0xFFE1E4E2),
    inverseSurface = White,
    inverseOnSurface = DarkGray
)

@Composable
fun FinancialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = if (darkTheme) TypographyDark else TypographyLite,
        content = content
    )
}
