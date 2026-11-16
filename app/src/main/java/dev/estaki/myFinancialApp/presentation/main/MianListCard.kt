package dev.estaki.myFinancialApp.presentation.main

import dev.estaki.ui_utils.localized

import androidx.compose.foundation.background
import dev.estaki.ui_utils.LocalDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.estaki.domain.models.SmsModel
import dev.estaki.domain.models.CategoryModel
import dev.estaki.domain.models.TransactionType
import dev.estaki.kt_pure_utils.formatAmountForDisplay
import dev.estaki.ui_utils.localizedDigits
import dev.estaki.ui_utils.AppAppearance
import dev.estaki.ui_utils.AppLanguage
import dev.estaki.ui_utils.ui.theme.AppSpacing
import dev.estaki.myFinancialApp.presentation.category.categoryIcon

@Composable
fun MyCardItem(smsModel: SmsModel, categories: List<CategoryModel> = emptyList(), onCardClick: () -> Unit) {
    val isIncome = smsModel.transactionType == TransactionType.DEPOSIT
    val accent = if (isIncome) {
        if (LocalDarkTheme.current) Color(0xFF70D7B3) else Color(0xFF087858)
    } else {
        if (LocalDarkTheme.current) Color(0xFFFFA7A5) else Color(0xFFB84045)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
        Card(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).then(smsItemInteraction(smsModel, onCardClick)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(AppSpacing.Medium),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(accent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isIncome) "↙" else "↗",
                        color = accent,
                        fontSize = 24.sp,
                        lineHeight = 28.sp
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (isIncome) localized("واریز") else localized("برداشت"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = smsModel.bankName.localizedDigits(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOf(smsModel.transactionDate, smsModel.transactionTime)
                            .filter { it.isNotBlank() && it != "-" }.joinToString("  ·  ").localizedDigits(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (smsModel.categoryIds.isNotEmpty()) {
                        TransactionCategories(smsModel.categoryIds.distinct(), categories)
                    }
                }
                Column(modifier = Modifier.widthIn(max = 128.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = smsModel.transactionAmount.formatAmountForDisplay(persianDigits = AppAppearance.language == AppLanguage.PERSIAN),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                    Text(
                        text = localized("ریال"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionCategories(categoryIds: List<Long>, categories: List<CategoryModel>) {
    val titles = categories.associateBy { it.id }
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.ExtraSmall),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.ExtraSmall),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.ExtraSmall)
    ) {
        categoryIds.forEach { id ->
            val title = localized(titles[id]?.title?.trim() ?: "دسته بندی")
            Surface(modifier = Modifier.widthIn(max = 140.dp), shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(categoryIcon(id), contentDescription = null, modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(title, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyCardItemPreview() {
    MyCardItem(
        smsModel = SmsModel(
            id = 1L,
            bankName = localized("بانک نمونه"),
            bankAccountNumber = "1234",
            transactionType = TransactionType.DEPOSIT,
            transactionAmount = "1250000",
            transactionDate = localized("۱۴۰۳/۰۱/۰۱"),
            transactionDateTime = 0L,
            transactionTime = localized("۱۲:۳۰"),
            bankCardBalance = "0",
            categoryIds = emptyList(),
            description = null,
            smsSender = "",
            smsBody = ""
        ),
        onCardClick = {}
    )
}
