package dev.estaki.myFinancialApp.presentation.smsimport

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.estaki.domain.models.TransactionType
import dev.estaki.kt_pure_utils.arabicToDecimal
import dev.estaki.myFinancialApp.presentation.main.MonifyAppearance
import dev.estaki.ui_utils.AppAppearance
import dev.estaki.ui_utils.AppLanguage
import dev.estaki.ui_utils.localizedDigits

@AndroidEntryPoint
class SmsImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MonifyAppearance(this) { SmsImportScreen(onClose = ::finish) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmsImportScreen(onClose: () -> Unit, viewModel: SmsImportViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val fa = AppAppearance.language == AppLanguage.PERSIAN
    var permissionDenied by remember { mutableStateOf(false) }
    var search by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<ImportMessage?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionDenied = !granted
        if (granted) viewModel.loadSenders()
    }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            viewModel.loadSenders()
        } else permission.launch(Manifest.permission.READ_SMS)
    }
    val back = { if (state.sender == null) onClose() else viewModel.backToSenders() }
    BackHandler(enabled = state.sender != null && !state.isSaving) { back() }
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    if (state.sender == null) { if (fa) "انتخاب فرستنده" else "Select sender" } else state.sender.orEmpty().localizedDigits(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = { IconButton(onClick = back, enabled = !state.isSaving) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (fa) "بازگشت" else "Back") } }
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (state.error) {
                Text(if (fa) "عملیات انجام نشد. دوباره تلاش کنید." else "Something went wrong. Please try again.", color = MaterialTheme.colorScheme.error)
                if (state.sender == null) TextButton(onClick = viewModel::loadSenders) { Text(if (fa) "تلاش دوباره" else "Retry") }
            }
            if (permissionDenied) {
                Text(if (fa) "برای نمایش پیامک‌ها، دسترسی پیامک لازم است." else "SMS access is needed to show your messages.")
                TextButton(onClick = { permission.launch(Manifest.permission.READ_SMS) }) { Text(if (fa) "درخواست دسترسی" else "Request access") }
                TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }) {
                    Text(if (fa) "تنظیمات دسترسی برنامه" else "App permission settings")
                }
            } else if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.sender == null) {
                Text(
                    if (fa) "فرستنده پیامک‌های بانکی را انتخاب کنید." else "Choose the sender of your bank messages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(if (fa) "جست‌وجوی فرستنده" else "Search senders", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(20.dp))
                val senders = state.senders.filter { it.address.contains(search.arabicToDecimal(), ignoreCase = true) }
                Text(
                    if (fa) "فرستنده‌ها" else "Senders",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                if (senders.isEmpty()) Text(
                    if (fa) "فرستنده‌ای پیدا نشد." else "No senders found.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)) {
                    items(senders, key = { it.address }) { sender ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.selectSender(sender.address) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Text(
                                    sender.address.localizedDigits(),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    (if (fa) "${sender.count} پیامک" else "${sender.count} messages").localizedDigits(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                val ready = state.messages.count { !it.imported && it.transaction != null && it.transaction.bankAccountNumber.count(Char::isDigit) >= 4 }
                Text(if (fa) "تراکنش‌های شناسایی‌شده را وارد کنید؛ پیامک‌های دیگر را می‌توانید بررسی و دستی ثبت کنید." else "Import recognized transactions or review and add other messages manually.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Button(onClick = viewModel::importRecognized, enabled = ready > 0 && !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                    Text((if (fa) "ورود تراکنش‌های شناسایی‌شده ($ready)" else "Import recognized transactions ($ready)").localizedDigits())
                }
                state.lastImportedCount?.let { Text((if (fa) "$it تراکنش وارد شد." else "$it transactions imported.").localizedDigits(), style = MaterialTheme.typography.bodySmall) }
                if (state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(state.messages, key = { it.raw._id }) { message ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val status = when {
                                    message.imported -> if (fa) "ثبت شده" else "Imported"
                                    message.transaction == null -> if (fa) "شناسایی نشده" else "Not recognized"
                                    message.transaction.bankAccountNumber.count(Char::isDigit) < 4 -> if (fa) "نیازمند شماره حساب" else "Account required"
                                    else -> if (fa) "تراکنش شناسایی شد" else "Transaction recognized"
                                }
                                Text(status, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                                Text(message.raw.description.localizedDigits(), style = MaterialTheme.typography.bodyMedium)
                                if (!message.imported) TextButton(onClick = { selected = message }, enabled = !state.isSaving) { Text(if (fa) "ثبت دستی" else "Add manually") }
                            }
                        }
                    }
                }
            }
        }
    }
    selected?.let { message ->
        ManualImportDialog(message, fa, onDismiss = { selected = null }) { bank, account, type, amount ->
            viewModel.importManual(message.raw, bank, account, type, amount)
            selected = null
        }
    }
}

@Composable
private fun ManualImportDialog(message: ImportMessage, fa: Boolean, onDismiss: () -> Unit, onSave: (String, String, TransactionType, String) -> Unit) {
    var bank by remember(message.raw._id) { mutableStateOf(message.transaction?.bankName ?: message.raw.senderName) }
    var account by remember(message.raw._id) { mutableStateOf(message.transaction?.bankAccountNumber?.takeUnless { it == "-" }.orEmpty()) }
    var amount by remember(message.raw._id) { mutableStateOf(message.transaction?.transactionAmount.orEmpty()) }
    var type by remember(message.raw._id) { mutableStateOf(message.transaction?.transactionType) }
    val normalizedAccount = account.arabicToDecimal()
    val normalizedAmount = amount.arabicToDecimal().filter(Char::isDigit)
    val valid = bank.isNotBlank() && normalizedAccount.count(Char::isDigit) >= 4 &&
        normalizedAccount.all { it.isDigit() || it in "*×- " } &&
        amount.arabicToDecimal().all { it.isDigit() || it in ",،٬ " } &&
        normalizedAmount.any { it != '0' } && type != null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (fa) "ثبت دستی تراکنش" else "Add transaction manually") },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (fa) "فقط پیامک مربوط به تراکنش قطعی را ثبت کنید." else "Only add messages for completed transactions.", style = MaterialTheme.typography.bodySmall)
                Text(message.raw.description.localizedDigits(), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(bank, { bank = it }, label = { Text(if (fa) "نام بانک" else "Bank name") }, singleLine = true)
                OutlinedTextField(account, { account = it }, label = { Text(if (fa) "شماره حساب یا کارت" else "Account or card number") }, singleLine = true)
                OutlinedTextField(amount, { amount = it }, label = { Text(if (fa) "مبلغ" else "Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == TransactionType.DEPOSIT, onClick = { type = TransactionType.DEPOSIT }, label = { Text(if (fa) "واریز" else "Deposit") })
                    FilterChip(selected = type == TransactionType.WITHDRAW, onClick = { type = TransactionType.WITHDRAW }, label = { Text(if (fa) "برداشت" else "Withdrawal") })
                }
            }
        },
        confirmButton = { TextButton(enabled = valid, onClick = { onSave(bank.trim(), normalizedAccount.trim(), requireNotNull(type), normalizedAmount) }) { Text(if (fa) "ثبت" else "Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(if (fa) "انصراف" else "Cancel") } }
    )
}
