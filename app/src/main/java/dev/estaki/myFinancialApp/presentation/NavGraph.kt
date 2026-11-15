package dev.estaki.myFinancialApp.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
            TopAppBar(
                title = {
                    if (isMainScreen) {
                        MonifyTopBarTitle()
                    } else {
                        Text(
                            text = topAppbarState.title,
                            fontFamily = ariaFaNumFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                },
                navigationIcon = topAppbarState.navigationIcon,
                actions = topAppbarState.actions,
                scrollBehavior = topAppbarState.scrollBehavior
            )
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
private fun MonifyTopBarTitle() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.monify_mark),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Column {
            Text(
                text = "مانیفای",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "همراه دخل و خرج تو",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
