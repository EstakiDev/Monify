package dev.estaki.myFinancialApp.presentation.detailScreen

import dev.estaki.ui_utils.localized

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import dev.estaki.ui_utils.LocalDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ehsanmsz.mszprogressindicator.progressindicator.BallPulseProgressIndicator
import com.gmail.hamedvakhide.compose_jalali_datepicker.JalaliDatePickerDialog
import dev.estaki.domain.error.MyCustomSnackBarType
import dev.estaki.domain.models.BankCardModel
import dev.estaki.domain.models.SmsModel
import dev.estaki.domain.models.TransactionType
import dev.estaki.kt_pure_utils.resetErr
import dev.estaki.kt_pure_utils.toAmountOrNull
import dev.estaki.ui_utils.localizedDigits
import dev.estaki.ui_utils.AppAppearance
import dev.estaki.ui_utils.AppLanguage
import androidx.compose.ui.text.input.VisualTransformation
import dev.estaki.myFinancialApp.presentation.actions.AddCreditCardActions
import dev.estaki.myFinancialApp.presentation.actions.TransactionDetailScreenActions
import dev.estaki.myFinancialApp.presentation.category.CategorySelector
import dev.estaki.myFinancialApp.presentation.main.MyCardItem
import dev.estaki.myFinancialApp.presentation.states.MyTopAppBarState
import dev.estaki.myFinancialApp.presentation.states.TransactionDetailScreenState
import dev.estaki.myFinancialApp.presentation.timepicker.MyTimePicker
import dev.estaki.ui_utils.R
import dev.estaki.ui_utils.SnackBarController
import dev.estaki.ui_utils.SnackBarEvent
import dev.estaki.ui_utils.components.AmountTextField
import dev.estaki.ui_utils.components.MyAlertDialog
import dev.estaki.ui_utils.components.MyOutlinedButton
import dev.estaki.ui_utils.ui.theme.AppSpacing
import dev.estaki.ui_utils.ui.theme.ColorTextGrayOnDarkTheme
import dev.estaki.ui_utils.ui.theme.ColorTextGrayOnLiteTheme
import dev.estaki.ui_utils.ui.theme.LiteRed
import dev.estaki.ui_utils.ui.theme.ariaFaNumFontFamily
import dev.estaki.ui_utils.utils.PersianDigitsTransformation
import ir.huri.jcal.JalaliCalendar
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetail(
    detailScreenViewModel: TransactionDetailScreenViewModel = hiltViewModel(),
    navController: NavHostController? = null,
    smsId: Long,
    onComposing: (MyTopAppBarState) -> Unit
) {
    val state by detailScreenViewModel.state.collectAsState()
    var showWarningDialog by remember {
        mutableStateOf(false)
    }
    if (showWarningDialog) {
        MyAlertDialog(
            onDismissRequest = {
                showWarningDialog =false
            },
            onConfirmation = {
                state.smsModel?.let {
                    detailScreenViewModel.onAction(TransactionDetailScreenActions.DeleteTransaction(it))
                }
                showWarningDialog =false
            },
            localized("هشدار"),
            localized("از حذف کردن این تراکنش بانکی اطمینان دارید؟"),
            icon = Icons.Rounded.Warning
        )
    }
    DisposableEffect(smsId, state.smsModel?.id, AppAppearance.language) {
        onComposing(
            MyTopAppBarState(
                title = if (smsId != 0L) localized("ویرایش اطلاعات تراکنش") else localized("افزودن تراکنش جدید"),
                navigationIcon = {
                    IconButton(onClick = {
                        navController?.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                actions = {
                    state.smsModel?.let {
                        IconButton(onClick = {
                            showWarningDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "delete"
                            )
                        }
                    }

                },
            )
        )
        onDispose {}
    }

    LaunchedEffect(smsId) {
        detailScreenViewModel.onAction(
            TransactionDetailScreenActions.LoadTransaction(
                smsId = smsId
            )
        )
    }
    LaunchedEffect(state.operationCompleted) {
        if (state.operationCompleted) navController?.navigateUp()
    }
    if (state.isError) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AppSpacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (AppAppearance.language == AppLanguage.PERSIAN) "بارگذاری تراکنش انجام نشد." else "Could not load the transaction.",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = { detailScreenViewModel.onAction(TransactionDetailScreenActions.LoadTransaction(smsId)) }) {
                Text(if (AppAppearance.language == AppLanguage.PERSIAN) "تلاش دوباره" else "Try again")
            }
        }
    } else {
        TransactionDetailUi(onAction = detailScreenViewModel::onAction, navController = navController, state = state)
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailUi(
    modifier: Modifier = Modifier,
    state: TransactionDetailScreenState,
    onAction: (TransactionDetailScreenActions) -> Unit,
    navController: NavHostController? = null
) {

    CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {

        val scrollState = rememberScrollState()
        var selectedTransactionType by remember { mutableStateOf(TransactionType.DEPOSIT) }
        var bankCardDropDownExpanded by remember { mutableStateOf(false) }
        var menuItemData by remember { mutableStateOf<List<BankCardModel>>(state.bankCardList) }
        var bankAccountNumber by remember {
            mutableStateOf(
                state.smsModel?.bankAccountNumber ?: ""
            )
        }
        var amountErr by remember { mutableStateOf<Pair<Boolean, String>>(Pair(false, "")) }
        var bankNameErr by remember { mutableStateOf<Pair<Boolean, String>>(Pair(false, "")) }
        var dateErr by remember { mutableStateOf<Pair<Boolean, String>>(Pair(false, "")) }
        var timeErr by remember { mutableStateOf<Pair<Boolean, String>>(Pair(false, "")) }
        var bankAccountNumberErr by remember {
            mutableStateOf<Pair<Boolean, String>>(
                Pair(
                    false, ""
                )
            )
        }

        var text by rememberSaveable {
            mutableStateOf("")
        }
        var localCategoryList by remember { mutableStateOf(state.categoryList) }
        LaunchedEffect(state.categoryList, state.smsModel?.id) {
            val selectedIds = state.smsModel?.categoryIds.orEmpty().toSet()
            localCategoryList = state.categoryList.map { it.copy(isChecked = it.id in selectedIds) }
        }
        LaunchedEffect(key1 = state.bankCardList) {
            menuItemData = state.bankCardList
        }

        LaunchedEffect(key1 = state.smsModel) {
            bankAccountNumber = state.smsModel?.bankAccountNumber ?: ""
        }
        val context = LocalContext.current
        var amount by remember {
            mutableStateOf(
                TextFieldValue(
                    state.smsModel?.transactionAmount?.toAmountOrNull()?.toString() ?: "",
                    selection = TextRange(state.smsModel?.transactionAmount?.toAmountOrNull()?.toString()?.length ?: 0)
                )
            )
        }
        var bankName by remember { mutableStateOf(state.smsModel?.bankName ?: "") }
        var time by remember { mutableStateOf(state.smsModel?.transactionTime ?: "") }
        var date by remember { mutableStateOf(state.smsModel?.transactionDate ?: "") }
        val coroutine = rememberCoroutineScope()
        val datePickerState = remember { mutableStateOf(false) }
        val timePickerState = remember { mutableStateOf(false) }
        var initialDateForDatePicker by remember {
            mutableStateOf(listOf<String>())
        }
        val dateInteractionSource = remember {
            MutableInteractionSource()
        }
        val timeInteractionSource = remember {
            MutableInteractionSource()
        }
        val segmentedButtonList = mapOf<TransactionType, String>(
            TransactionType.DEPOSIT to localized("دخل"), TransactionType.WITHDRAW to localized("خرج")
        )


        val newSmsModel = state.smsModel?.copy(
            transactionAmount = amount.text,
            bankName = bankName,
            transactionTime = time,
            transactionDate = date,
            transactionType = selectedTransactionType
        ) ?: SmsModel(
            id = null,
            bankName = bankName,
            bankAccountNumber = bankAccountNumber,
            transactionType = selectedTransactionType,
            transactionAmount = amount.text,
            transactionDate = date,
            transactionDateTime = Date(System.currentTimeMillis()).time,
            transactionTime = time,
            bankCardBalance = "",
            listOf(),
            null,
            "",
            ""
        )


        LaunchedEffect(key1 = state.smsModel) {
            state.smsModel?.let {
                val normalizedAmount = it.transactionAmount.toAmountOrNull()?.toString() ?: it.transactionAmount
                amount = TextFieldValue(normalizedAmount, selection = TextRange(normalizedAmount.length))
                bankName = it.bankName
                time = it.transactionTime
                date = it.transactionDate
                text = it.description ?: ""
                selectedTransactionType = it.transactionType
            }

        }

        Box(
            Modifier.fillMaxSize(),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .alpha(if (state.isLoading) 0f else 1f)
                    .padding(bottom = 92.dp)
            ) {
                Spacer(Modifier.size(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.Large)
                ) {
                    MyCardItem(smsModel = newSmsModel) { }

                    Spacer(modifier = Modifier.size(AppSpacing.Large))
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.Medium)) {
                        AmountTextField(
                            amount = amount,
                            modifier = Modifier.fillMaxWidth(),
                            unit = localized("ريال"),
                            isError = amountErr.first,
                            supportingText = if (amountErr.first) {
                                { Text(text = amountErr.second) }
                            } else null

                        ) {
                            Timber.tag("TAG").d("length: ${it.selection.length}")
                            Timber.tag("TAG").d("length+1: ${it.selection.length + 1}")
                            amount = it
                        }
                        OutlinedTextField(
                            value = bankName,
                            visualTransformation = if (AppAppearance.language == AppLanguage.PERSIAN) PersianDigitsTransformation else VisualTransformation.None,
                            onValueChange = {
                                bankName = it
                            },
                            label = {
                                Text(
                                    localized("نام بانک"), style = TextStyle(
                                        fontSize = 14.sp, fontFamily = ariaFaNumFontFamily
                                    ), fontWeight = FontWeight.Medium
                                )
                            },
                            isError = bankNameErr.first,
                            readOnly = true,
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            supportingText = if (bankNameErr.first) {
                                { Text(text = bankNameErr.second) }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = ariaFaNumFontFamily,
                                fontWeight = FontWeight.Medium
                            ),

                            )
                    }
                    Spacer(modifier = Modifier.size(AppSpacing.Medium))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.Medium)) {

                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            visualTransformation = if (AppAppearance.language == AppLanguage.PERSIAN) PersianDigitsTransformation else VisualTransformation.None,
                            interactionSource = dateInteractionSource,
                            value = date,
                            onValueChange = {
                                date = it
                            },
                            readOnly = true,
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            label = {
                                Text(
                                    text = localized("تاریخ"), style = TextStyle(
                                        fontSize = 14.sp, fontFamily = ariaFaNumFontFamily
                                    ), fontWeight = FontWeight.Medium
                                )
                            },
                            isError = dateErr.first,
                            supportingText = if (dateErr.first) {
                                { Text(text = dateErr.second) }
                            } else null,
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = ariaFaNumFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        if (dateInteractionSource.collectIsPressedAsState().value) {
                            initialDateForDatePicker = date.split("/")
                            datePickerState.value = true

                        }

                        OutlinedTextField(
                            value = time,
                            visualTransformation = if (AppAppearance.language == AppLanguage.PERSIAN) PersianDigitsTransformation else VisualTransformation.None,
                            onValueChange = {
                                time = it
                            },
                            readOnly = true,
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            interactionSource = timeInteractionSource,
                            label = {
                                Text(
                                    localized("ساعت"), style = TextStyle(
                                        fontSize = 14.sp, fontFamily = ariaFaNumFontFamily
                                    ), fontWeight = FontWeight.Medium
                                )
                            },
                            isError = timeErr.first,
                            supportingText = if (timeErr.first) {
                                { Text(text = timeErr.second) }
                            } else null,
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = ariaFaNumFontFamily,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        if (timeInteractionSource.collectIsPressedAsState().value) {
                            timePickerState.value = true
                        }


                    }
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        if (initialDateForDatePicker.isNotEmpty() && initialDateForDatePicker.size == 3) JalaliDatePickerDialog(
                            openDialog = datePickerState,
                            initialDate = JalaliCalendar(
                                initialDateForDatePicker[0].toInt(),
                                initialDateForDatePicker[1].toInt(),
                                initialDateForDatePicker[2].toInt()
                            ),
                            onSelectDay = { //it:JalaliCalendar
                                Timber.tag("Date")
                                    .d("onSelect: ${it.day} ${it.monthString} ${it.year}")
                            },
                            onConfirm = {
                                Timber.tag("Date")
                                    .d("onConfirm: ${it.day} ${it.monthString} ${it.year}")
                                date = "${it.year}/${it.month}/${it.day}"
                            },
                            fontFamily = FontFamily(
                                Font(R.font.vazirmatn_bold)
                            ),
                            fontSize = 17.sp,
                        )
                        else JalaliDatePickerDialog(
                            openDialog = datePickerState,
                            onSelectDay = { //it:JalaliCalendar
                                Timber.tag("Date")
                                    .d("onSelect: ${it.day} ${it.monthString} ${it.year}")
                            },
                            onConfirm = {
                                Timber.tag("Date")
                                    .d("onConfirm: ${it.day} ${it.monthString} ${it.year}")
                                date = "${it.year}/${it.month}/${it.day}"
                            },
                            fontFamily = FontFamily(
                                Font(R.font.vazirmatn_bold)
                            ),
                            fontSize = 17.sp,
                        )
                    }

                    if (timePickerState.value) MyTimePicker(onConfirm = {
                        timePickerState.value = false
                        Timber.tag("TAG")
                            .d("CreateNewDetail: hour: ${it.hour} minute ${it.minute} ")
                        time =
                            "${if (it.hour.toString().length == 1) "0${it.hour}" else it.hour}:${it.minute}"
                    }) {
                        timePickerState.value = false
                    }

                }
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.Large, vertical = AppSpacing.Medium)
                ) {
                    segmentedButtonList.forEach { item ->
                        SegmentedButton(selected = selectedTransactionType == item.key, onClick = {
                            selectedTransactionType = item.key
                        }, enabled = true, shape = RoundedCornerShape(10.dp), icon = {
                            Icon(
                                painter = painterResource(if (item.key == TransactionType.DEPOSIT) R.drawable.ic_income_32 else R.drawable.ic_expenses_32),
                                contentDescription = "income",
                                tint = if (item.key == TransactionType.DEPOSIT) Color.Green else Color.Red
                            )
                        }) {
                            Text(
                                text = item.value, style = TextStyle(
                                    fontSize = 15.sp,
                                    fontFamily = ariaFaNumFontFamily,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.Large)) {
                    Text(
                        text = localized("انتخاب حساب"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = AppSpacing.Small)
                    )
                    ExposedDropdownMenuBox(
                        expanded = bankCardDropDownExpanded,
                        onExpandedChange = { bankCardDropDownExpanded = it }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(AppSpacing.Large),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (bankAccountNumber.isBlank()) localized("انتخاب کنید") else bankName.localizedDigits(),
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (bankAccountNumber.isNotBlank()) {
                                        Text(
                                            text = bankAccountNumber.localizedDigits(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankCardDropDownExpanded)
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = bankCardDropDownExpanded,
                            onDismissRequest = { bankCardDropDownExpanded = false },
                            modifier = Modifier.heightIn(max = 320.dp),
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            if (menuItemData.isEmpty()) {
                                Text(
                                    text = localized("حسابی برای انتخاب وجود ندارد"),
                                    modifier = Modifier.padding(AppSpacing.Large),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            menuItemData.forEach { bankCard ->
                                val isSelected = bankCard.bankAccountNumber == bankAccountNumber
                                DropdownMenuItem(
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.ExtraSmall)) {
                                            Text(
                                                text = bankCard.bankName.localizedDigits(),
                                                style = MaterialTheme.typography.titleSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = bankCard.bankAccountNumber.localizedDigits(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = bankCard.bankName.trim().firstOrNull()?.toString()?.localizedDigits() ?: localized("؟"),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    trailingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = localized("حساب انتخاب‌شده"), tint = MaterialTheme.colorScheme.primary) }
                                    } else null,
                                    onClick = {
                                        bankAccountNumber = bankCard.bankAccountNumber
                                        bankName = bankCard.bankName
                                        bankAccountNumberErr = resetErr()
                                        bankCardDropDownExpanded = false
                                    },
                                    modifier = Modifier.padding(horizontal = AppSpacing.ExtraSmall)
                                )
                            }
                        }
                    }
                    AnimatedVisibility(bankAccountNumberErr.first) {
                        Text(
                            text = bankAccountNumberErr.second,
                            modifier = Modifier.padding(start = AppSpacing.Large, top = AppSpacing.ExtraSmall),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                CategorySelector(categories = localCategoryList) { selectedId ->
                    localCategoryList = localCategoryList.map { category ->
                        if (category.id == selectedId) category.copy(isChecked = !category.isChecked) else category
                    }
                }

                OutlinedTextField(
                    visualTransformation = if (AppAppearance.language == AppLanguage.PERSIAN) PersianDigitsTransformation else VisualTransformation.None,
                    modifier = Modifier
                        .padding(AppSpacing.Large)
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontFamily = ariaFaNumFontFamily,
                        fontWeight = FontWeight.Medium
                    ),
                    placeholder = {
                        Text(
                            fontFamily = ariaFaNumFontFamily,
                            fontWeight = FontWeight.Normal,
                            text = localized("توضیحات تراکنشت رو اینجا وارد کن..."),
                            fontSize = 14.sp
                        )
                    },
                    label = {
                        Text(
                            fontFamily = ariaFaNumFontFamily,
                            fontWeight = FontWeight.Medium,
                            text = localized("توضیحات"),
                            fontSize = 14.sp,
                        )
                    },
                    value = text,
                    onValueChange = {
                        text = it
                    })
            }


            MyOutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.Large)
                    .align(Alignment.BottomCenter)
                    .alpha(if (state.isLoading) 0f else 1f), text = localized("ذخیره")
            ) {

                bankNameErr = resetErr()
                amountErr = resetErr()
                bankAccountNumberErr = resetErr()
                dateErr = resetErr()
                timeErr = resetErr()

                if (bankName.isBlank()) {
                    bankNameErr = Pair(true, localized("نام بانک نمیتواند خالی باشد"))
                }
                if (amount.text.isBlank()) {
                    amountErr = Pair(true, localized("مبلغ نمیتواند خالی باشد"))
                }
                if (bankAccountNumber.isBlank()) {
                    bankAccountNumberErr = Pair(true, localized("شماره حساب نمیتواند خالی باشد"))
                }
                if (date.isBlank()) {
                    dateErr = Pair(true, localized("تاریخ نمیتواند خالی باشد"))
                }
                if (time.isBlank()) {
                    timeErr = Pair(true, localized("ساعت تراکنش نمیتواند خالی باشد"))
                }
                if (amountErr.first || bankNameErr.first || dateErr.first || timeErr.first || bankAccountNumberErr.first) {
                    coroutine.launch {
                        SnackBarController.sendEvent(
                            SnackBarEvent(
                                localized("متاسفانه عملیات مورد نظر با خطا مواجه شد! \n لطفا مقادیر خالی را پرکنید. "),
                                type = MyCustomSnackBarType.ERROR
                            )
                        )
                    }


                } else {
                    newSmsModel.let { sms ->

                        onAction.invoke(
                            TransactionDetailScreenActions.SaveTransaction(
                                sms.copy(
                                    transactionAmount = amount.text,
                                    bankAccountNumber = bankAccountNumber,
                                    transactionDate = date,
                                    transactionTime = time,
                                    bankName = bankName,
                                    description = text,
                                    categoryIds = localCategoryList.filter { it.isChecked }
                                        .map { it.id },
                                    transactionType = selectedTransactionType,
                                    isModified = true
                                )
                            )
                        )

                    }
                }

            }

            BallPulseProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(0.dp, 32.dp)
                    .alpha(if (state.isLoading) 1f else 0f),
                color = if (LocalDarkTheme.current) ColorTextGrayOnDarkTheme else ColorTextGrayOnLiteTheme,
                animationDuration = 800,
                animationDelay = 200,
                startDelay = 0,
                ballCount = 3,
                maxBallDiameter = 17.dp

            )

        }

    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddDetailScreenPreview() {
    TransactionDetail(smsId = 0L) {}
}
