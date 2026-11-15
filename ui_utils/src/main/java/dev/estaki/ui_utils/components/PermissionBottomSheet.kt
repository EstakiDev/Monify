package dev.estaki.ui_utils.components

import dev.estaki.ui_utils.localized

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import dev.estaki.ui_utils.ui.theme.AppSpacing
import dev.estaki.ui_utils.ui.theme.ariaFaNumFontFamily


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionBottomSheet(
    modifier: Modifier = Modifier,
    modalBottomSheetState: SheetState,
    onBtnGetPermissionClicked: () -> Unit,
    onDismissRequest: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {

        ModalBottomSheet(
            onDismissRequest = onDismissRequest
            ,
            sheetState = modalBottomSheetState
        ) {

            Column(
                modifier = Modifier
                    .padding(AppSpacing.Large)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = AppSpacing.Small),
                    text = localized("سلام رفیق\n اپلیکیشن برای این که بتونه پیامک تراکنش های بانکی تو رو بهت نشون بده نیاز داره که تو این دسترسی رو تایید کنی."),
                    fontFamily = ariaFaNumFontFamily,
                    fontSize = 15.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Bold,
                )

                MyOutlinedButton(
                    modifier = modifier.padding(vertical = AppSpacing.ExtraLarge),
                    text = localized("تایید دسترسی")
                ) {
                    onBtnGetPermissionClicked.invoke()
                }

            }
        }
    }
}
