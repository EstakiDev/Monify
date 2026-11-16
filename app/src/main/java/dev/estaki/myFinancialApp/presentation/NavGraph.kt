package dev.estaki.myFinancialApp.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.navigation.navArgument
import dev.estaki.domain.error.MyCustomSnackBarType
import dev.estaki.monify.R
import dev.estaki.myFinancialApp.presentation.addAndEditBankCard.AddOrEditCreditCardScreen
import dev.estaki.myFinancialApp.presentation.detailScreen.TransactionDetail
import dev.estaki.myFinancialApp.presentation.main.MainScreen
import dev.estaki.myFinancialApp.presentation.splash.MySplashScreen
import dev.estaki.myFinancialApp.presentation.splash.SplashScreenViewModel
import dev.estaki.myFinancialApp.presentation.states.MyTopAppBarState
import dev.estaki.ui_utils.SnackBarController
import dev.estaki.ui_utils.AppAppearance
import dev.estaki.ui_utils.AppLanguage
import dev.estaki.ui_utils.localized
import dev.estaki.ui_utils.SnackBarEvent
import dev.estaki.ui_utils.components.MyCustomSnackBar
import dev.estaki.ui_utils.components._observeAsState
import dev.estaki.ui_utils.ui.theme.ariaFaNumFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Nav(modifier: Modifier = Modifier, navController: NavHostController) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val splashViewModel: SplashScreenViewModel = hiltViewModel()
    val splashState by splashViewModel.splashScreenState.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isMainScreen = currentBackStackEntry?.destination?.route == "MainScreen"
    val isPrivacyScreen = currentBackStackEntry?.destination?.route == "PrivacyPolicy"

    var topAppbarState by remember {
        mutableStateOf(
            MyTopAppBarState(
                title = "مانیفای",
                scrollBehavior = scrollBehavior
            )
        )
    }

    val snackBarHostState = remember {
        SnackbarHostState()
    }
    val scope = rememberCoroutineScope()

    val a by _observeAsState(
        initialValue = SnackBarEvent(message = "", type = MyCustomSnackBarType.SUCCESS),
        flow = SnackBarController.events,
        snackBarHostState
    ) { event ->
        scope.launch {
            snackBarHostState.currentSnackbarData?.dismiss()
            val result = snackBarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.action?.name,
                duration = SnackbarDuration.Long,
               // withDismissAction = true
            )

            if (result == SnackbarResult.ActionPerformed) {
                event.action?.action?.invoke()
            }
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBarHostState) { snackBarData: SnackbarData ->
                MyCustomSnackBar(snackBarEvent = a, type = a.type){
                    snackBarData.performAction()
                }
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (isMainScreen) {
                MonifyHomeHeader(onPrivacyClick = { navController.navigate("PrivacyPolicy") })
            } else if (isPrivacyScreen) {
                TopAppBar(
                    title = { Text(if (AppAppearance.language == AppLanguage.PERSIAN) "حریم خصوصی" else "Privacy policy") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = if (AppAppearance.language == AppLanguage.PERSIAN) "بازگشت" else "Back")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = localized(topAppbarState.title),
                            fontFamily = ariaFaNumFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    },
                    navigationIcon = topAppbarState.navigationIcon,
                    actions = topAppbarState.actions,
                    scrollBehavior = topAppbarState.scrollBehavior
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            modifier = modifier.padding(innerPadding),
            navController = navController,
            startDestination = "SplashScreen"
        ) {
            composable(
                route = "SplashScreen",
            ) {
                MySplashScreen(navController = navController, viewModel = splashViewModel) {
                    topAppbarState = it
                }
            }
            composable(
                route = "MainScreen",
            ) {
                MainScreen(navController, isSyncing = splashState.isSyncing) {
                    topAppbarState = it
                }
            }
            composable(route = "PrivacyPolicy") {
                PrivacyPolicyScreen()
            }
            composable(
                route = "AddDetailScreen/{smsId}", arguments = listOf(
                    navArgument("smsId") {
                        type = NavType.LongType
                    }
                )) { backStackEntry ->
                val smsId = backStackEntry.arguments?.getLong("smsId") ?: return@composable
                TransactionDetail(
                    smsId = smsId,
                    navController = navController
                ) {
                    topAppbarState = it
                }
            }

            composable(
                route = "AddOrEditCreditCard/{creditAccountNumber}/{position}",
                arguments = listOf(
                    navArgument("creditAccountNumber") {
                        type = NavType.StringType
                    },
                    navArgument("position") {
                        type = NavType.IntType
                    }
                )) { backStackEntry ->
                val creditAccountNumber = backStackEntry.arguments?.getString("creditAccountNumber")
                    ?: return@composable
                val position = backStackEntry.arguments?.getInt("position") ?: return@composable
                AddOrEditCreditCardScreen(
                    creditAccountNumber = creditAccountNumber,
                    position = position,
                    navController = navController
                ) {
                    topAppbarState = it
                }
            }

        }
    }
}
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MonifyHomeHeader(onPrivacyClick: () -> Unit) {
    var showSettings by remember { mutableStateOf(false) }
    if (showSettings) AppearanceSettings(
        onDismiss = { showSettings = false },
        onPrivacyClick = {
            showSettings = false
            onPrivacyClick()
        }
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(TopAppBarDefaults.windowInsets)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val showWordmark = maxWidth >= 340.dp
        val shape = RoundedCornerShape(20.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Brush.horizontalGradient(listOf(Color(0xFF1A3041), Color(0xFF345667))))
                .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.monify_mark),
                contentDescription = null,
                modifier = Modifier.size(52.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localized("مانیفای"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAF8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = localized("حساب‌ها و تراکنش‌ها، یک‌جا"),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFD3E0E4),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (showWordmark) {
                Column(
                    modifier = Modifier.clearAndSetSemantics {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "MONIFY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFFD3E0E4)
                    )
                    Box(
                        modifier = Modifier.width(24.dp).height(2.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF8C577))
                    )
                }
            }
            IconButton(onClick = { showSettings = true }) {
                Icon(Icons.Rounded.Settings, contentDescription = if (AppAppearance.language == AppLanguage.PERSIAN) "تنظیمات" else "Settings", tint = Color.White)
            }
        }
    }
}
