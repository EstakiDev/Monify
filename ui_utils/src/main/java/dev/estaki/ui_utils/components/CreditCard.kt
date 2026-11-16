package dev.estaki.ui_utils.components

import dev.estaki.ui_utils.localized

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.estaki.domain.models.BankCardModel
import dev.estaki.kt_pure_utils.formatAmountForDisplay
import dev.estaki.ui_utils.R

private val CardGradients = listOf(
    listOf(Color(0xFF133B49), Color(0xFF102A3A), Color(0xFF101C32)),
    listOf(Color(0xFF443259), Color(0xFF2B2749), Color(0xFF1D203A)),
    listOf(Color(0xFF245447), Color(0xFF173F3A), Color(0xFF152D35)),
    listOf(Color(0xFF62463C), Color(0xFF44333B), Color(0xFF28273A))
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreditCard(
    modifier: Modifier = Modifier.wrapContentSize(),
    item: BankCardModel?,
    position: Int = 0,
    onLongClick: (() -> Unit)? = null,
    onClicked: () -> Unit
) {
    val colors = CardGradients[Math.floorMod(position, CardGradients.size)]
    val numberFont = FontFamily(Font(R.font.ocr_a))
    val cardNumber = item?.bankCardNumber
        ?.takeUnless { it.isBlank() || it == "null" }
        ?: "**** **** **** ****"
    val accountNumber = item?.bankAccountNumber?.ifBlank { "---" } ?: "---"
    val balance = item?.bankCardBalance?.formatAmountForDisplay(persianDigits = false)
        ?.ifBlank { "-" } ?: "-"
    val secondary = Color.White.copy(alpha = 0.69f)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Card(
            modifier = modifier.fillMaxSize().combinedClickable(onClick = onClicked, onLongClick = onLongClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.last())
        ) {
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(colors))) {
                Canvas(Modifier.fillMaxSize()) {
                    val center = Offset(size.width * 1.02f, size.height * 0.05f)
                    drawCircle(Color.White.copy(alpha = 0.035f), radius = size.width * 0.49f, center = center)
                    drawCircle(Color.White.copy(alpha = 0.07f), radius = size.width * 0.38f, center = center, style = Stroke(1.dp.toPx()))
                    drawCircle(Color.White.copy(alpha = 0.045f), radius = size.width * 0.31f, center = center, style = Stroke(1.dp.toPx()))
                }
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 17.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(localized("کارت اعتباری"), color = secondary, fontSize = 11.sp, lineHeight = 16.sp, maxLines = 1)
                            Text(
                                item?.bankName?.ifBlank { localized("نام بانک") } ?: localized("نام بانک"),
                                color = Color.White, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Image(painterResource(R.drawable.chip), contentDescription = null, modifier = Modifier.size(35.dp))
                    }
                    Column {
                        Text(localized("شماره کارت"), color = secondary, fontSize = 11.sp, lineHeight = 16.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            cardNumber, color = Color.White, fontFamily = numberFont,
                            fontSize = if (cardNumber.length > 20) 13.sp else 16.sp, lineHeight = 21.sp,
                            letterSpacing = if (cardNumber.length > 20) 0.sp else 1.sp,
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1.15f)) {
                            Text(localized("شماره حساب"), color = secondary, fontSize = 11.sp, lineHeight = 16.sp, maxLines = 1)
                            Spacer(Modifier.height(3.dp))
                            Text(
                                accountNumber, color = Color.White, fontFamily = numberFont,
                                fontSize = if (accountNumber.length > 20) 10.sp else 12.sp, lineHeight = 16.sp,
                                maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(localized(" موجودی: ").trim().trimEnd(':'), color = secondary, fontSize = 11.sp, lineHeight = 16.sp, maxLines = 1)
                            Spacer(Modifier.height(3.dp))
                            Text(
                                balance, color = Color.White, fontFamily = numberFont, fontSize = 12.sp, lineHeight = 16.sp,
                                maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CreditCardPreview() {
    CreditCard(modifier = Modifier, null) {}
}

@Composable
fun CreditCard(
    modifier: Modifier = Modifier.wrapContentSize(),
    bankName: String,
    bankAccountNumber: String,
    bankCardNumber: String,
    bankCardBalance: String,
    position: Int = 0,
    onClicked: () -> Unit
) {
    CreditCard(
        modifier = modifier,
        item = BankCardModel(
            id = 0,
            bankName = bankName,
            bankAccountNumber = bankAccountNumber,
            bankCardNumber = bankCardNumber,
            bankCardBalance = bankCardBalance
        ),
        position = position,
        onClicked = onClicked
    )
}
