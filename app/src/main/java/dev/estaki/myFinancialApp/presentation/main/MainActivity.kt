package dev.estaki.myFinancialApp.presentation.main

import android.os.Bundle
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.estaki.myFinancialApp.presentation.Nav
import dev.estaki.myFinancialApp.ui.theme.FinancialTheme
import dev.estaki.ui_utils.AppAppearance
import dev.estaki.ui_utils.AppLanguage
import dev.estaki.ui_utils.ThemeMode
import java.util.Locale


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonifyAppearance(this) {
                val navController = rememberNavController()
                Nav(navController = navController)
            }
        }
    }
}

@Composable
internal fun MonifyAppearance(activity: ComponentActivity, content: @Composable () -> Unit) {
    val darkTheme = when (AppAppearance.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val direction = if (AppAppearance.language == AppLanguage.PERSIAN) LayoutDirection.Rtl else LayoutDirection.Ltr
    val systemConfiguration = LocalConfiguration.current
    val locale = if (AppAppearance.language == AppLanguage.PERSIAN) Locale.forLanguageTag("fa") else Locale.ENGLISH
    val configuration = remember(systemConfiguration, locale) {
        Configuration(systemConfiguration).apply { setLocale(locale) }
    }
    val localizedContext = remember(activity, configuration) {
        ContextThemeWrapper(activity, activity.theme).apply {
            applyOverrideConfiguration(configuration)
        }
    }
    CompositionLocalProvider(
        LocalLayoutDirection provides direction,
        LocalConfiguration provides configuration,
        LocalContext provides localizedContext
    ) {
        FinancialTheme(darkTheme = darkTheme) { content() }
    }
}

