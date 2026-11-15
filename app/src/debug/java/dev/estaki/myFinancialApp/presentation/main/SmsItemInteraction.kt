package dev.estaki.myFinancialApp.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import dev.estaki.domain.models.SmsModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun smsItemInteraction(smsModel: SmsModel, onClick: () -> Unit): Modifier {
    val context = LocalContext.current
    var pendingReport by remember { mutableStateOf<SmsModel?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val sms = pendingReport
        pendingReport = null
        if (granted && sms != null) {
            SmsReportWriter.write(context.applicationContext, sms)
        } else if (!granted) {
            Toast.makeText(context, "مجوز ذخیره‌سازی گزارش داده نشد", Toast.LENGTH_LONG).show()
        }
    }
    return Modifier.combinedClickable(onClick = onClick, onLongClick = {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingReport = smsModel
            permission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            SmsReportWriter.write(context.applicationContext, smsModel)
        }
    })
}
