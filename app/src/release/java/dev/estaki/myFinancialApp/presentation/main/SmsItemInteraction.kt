package dev.estaki.myFinancialApp.presentation.main

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.estaki.domain.models.SmsModel

@Composable
internal fun smsItemInteraction(smsModel: SmsModel, onClick: () -> Unit): Modifier =
    Modifier.clickable(onClick = onClick)
