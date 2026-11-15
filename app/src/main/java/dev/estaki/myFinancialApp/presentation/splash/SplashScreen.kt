package dev.estaki.myFinancialApp.presentation.splash

import android.Manifest
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ehsanmsz.mszprogressindicator.progressindicator.BallPulseProgressIndicator
import dev.estaki.monify.R
import dev.estaki.myFinancialApp.isPermissionsGranted
import dev.estaki.myFinancialApp.presentation.actions.SplashScreenActions
import dev.estaki.myFinancialApp.presentation.states.MyTopAppBarState
import dev.estaki.myFinancialApp.presentation.states.SplashScreenState
import dev.estaki.ui_utils.components.MyAlertDialog
import dev.estaki.ui_utils.localized
import dev.estaki.ui_utils.components.PermissionBottomSheet
import dev.estaki.ui_utils.ui.theme.AppSpacing
import dev.estaki.ui_utils.utils.showAppSettings


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySplashScreen(
    modifier: Modifier = Modifier,
    viewModel: SplashScreenViewModel = hiltViewModel(),
    navController: NavHostController,
    onComposing: (MyTopAppBarState) -> Unit,
) {
    val splashScreenState by viewModel.splashScreenState.collectAsState()
    LaunchedEffect(true) {
        onComposing.invoke(MyTopAppBarState())
    }

    SplashScreenUi(
        modifier = modifier,
        navController = navController,
        onAction = viewModel::onAction,
        state = splashScreenState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreenUi(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onAction: (SplashScreenActions) -> Unit,
    state: SplashScreenState
) {
    val permissions = arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS
    )
    val modalBottomSheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val activity = LocalActivity.current
    var showManyFailedAttemptPermissions by remember { mutableStateOf(false) }
    var mustGetPermissions by remember { mutableStateOf(false) }
    var showDenyPermissions by remember { mutableStateOf(false) }
    var numberOfAttempts by remember { mutableIntStateOf(0) }

    LaunchedEffect(false) {
        if (isPermissionsGranted(context)) {
            onAction.invoke(SplashScreenActions.ExtractSmsFromContentResolver(context.contentResolver))
        } else {
//            onAction.invoke(SplashScreenActions.GetPermissions)
            mustGetPermissions = true
        }
    }
    LaunchedEffect(state.isFinishedAndGoToMainScreen, state.isLoading) {
        if (state.isFinishedAndGoToMainScreen) {
            navController.navigate("MainScreen") {
                popUpTo("SplashScreen") {
                    inclusive = true
                }
            }
        }
    }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            mustGetPermissions = false
            if (permissions.all { permission -> it[permission] == true }) {
                onAction.invoke(SplashScreenActions.ExtractSmsFromContentResolver(context.contentResolver))
            } else {
                checkShouldShowRequestPermissionRationale(activity, permissions) {
                    if (it || numberOfAttempts >= 2) {
                        showManyFailedAttemptPermissions = true
                    } else {
                        numberOfAttempts++
                        showDenyPermissions = true
                    }
                }

            }
        }
    )
    val alpha = remember {
        Animatable(0f)
    }
    LaunchedEffect(true) {
        alpha.animateTo(1f, animationSpec = tween(250))
    }


    if (mustGetPermissions) {
        PermissionBottomSheet(
            modifier = Modifier,
            modalBottomSheetState,
            onBtnGetPermissionClicked = {
                checkShouldShowRequestPermissionRationale(activity, permissions) {
                    if (it) {
                        mustGetPermissions = false
                        showManyFailedAttemptPermissions = true
                    } else {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_SMS,
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        )
                    }
                }
            },
            onDismissRequest = {
                mustGetPermissions = false
                showDenyPermissions = true
            }
        )
    } else {
        LaunchedEffect(true) {
            if (modalBottomSheetState.isVisible)
                modalBottomSheetState.hide()
        }
    }


    if (showDenyPermissions) {
        MyAlertDialog(
            onDismissRequest = {
                showDenyPermissions = true
            },
            onConfirmation = {
//                onAction.invoke(SplashScreenActions.GetPermissions)
                showDenyPermissions = false
                mustGetPermissions = true
            },
            localized("هشدار"),
            localized("متاسفانه شما دسترسی های اجباری مورد نیاز مانیفای رو تایید نکردید، لطفا دسترسی ها را تایید کنید."),
            icon = Icons.Rounded.Warning,
            hasDismissBtn = false,
            confirmButtonText = localized("بزن بریم")
        )
    }

    if (showManyFailedAttemptPermissions) {
        MyAlertDialog(
            onDismissRequest = {
                showManyFailedAttemptPermissions = false
            },
            onConfirmation = {
                activity?.showAppSettings()
                mustGetPermissions = false
                showManyFailedAttemptPermissions = false
            },
            localized("هشدار"),
            localized("متاسفانه بدلیل تلاش بیش از حد برای دریافت دسترسی ها، دریافت به صورت مستقیم امان پذیر نیست، لطفا با زدن دکنه زیر به تنظیمات اپلیکیشن رفته و از آن قسمت دسترسی های مورد نیاز اپ را تایید کنید."),
            icon = Icons.Rounded.Warning,
            hasDismissBtn = false,
            confirmButtonText = localized("بزن بریم")
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        MonifyBrandIntro(
            modifier = Modifier.align(Alignment.Center).alpha(alpha.value)
        )

        BallPulseProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(0.dp, 32.dp),
            color = MaterialTheme.colorScheme.primary,
            animationDuration = 800,
            animationDelay = 200,
            startDelay = 0,
            ballCount = 3,
            maxBallDiameter = 7.dp

        )

    }
}


@Composable
private fun MonifyBrandIntro(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = AppSpacing.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.monify_mark),
            contentDescription = null,
            modifier = Modifier.size(160.dp)
        )
        Text(
            text = localized("مانیفای"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = localized("یک نگاه روشن به دخل و خرجت"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SplashScreenPreview() {
    MonifyBrandIntro()
}

private fun checkShouldShowRequestPermissionRationale(
    activity: Activity?,
    listOfPermissions: Array<String>,
    onResult: (shouldShowRequestPermissionRationale: Boolean) -> Unit
) {
    if (activity == null) {
        onResult(false)
        return
    }
    var shouldShowRequestPermissionRationale = false
    listOfPermissions.forEach {
        shouldShowRequestPermissionRationale =
            shouldShowRequestPermissionRationale(activity, it)
        if (shouldShowRequestPermissionRationale)
            return@forEach
    }
    onResult.invoke(shouldShowRequestPermissionRationale)
}
