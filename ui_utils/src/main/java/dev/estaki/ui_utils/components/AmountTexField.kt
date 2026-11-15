package dev.estaki.ui_utils.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.estaki.kt_pure_utils.arabicToDecimal
import dev.estaki.kt_pure_utils.formatAmountForDisplay
import dev.estaki.ui_utils.ui.theme.ariaFaNumFontFamily

@Composable
fun AmountTextField(
    modifier: Modifier = Modifier,
    amount: TextFieldValue,
    label: String = "مبلغ",
    unit: String,
    isError: Boolean = false,
    supportingText : @Composable (()->Unit)? = null,
    onValueChange: (newAmount: TextFieldValue) -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier = modifier) {
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    val newString = it.text.arabicToDecimal().filter { character ->
                        character in '0'..'9' || character == '-' || character == '+'
                    }
                    if (!newString.matches(Regex("[+-]?[0-9]*"))) return@OutlinedTextField
                    fun normalizedOffset(offset: Int) = it.text.take(offset).count { character ->
                        character.isDigit() || character == '-' || character == '+'
                    }.coerceAtMost(newString.length)
                    val newValue = it.copy(
                        text = newString,
                        selection = TextRange(normalizedOffset(it.selection.start), normalizedOffset(it.selection.end))
                    )
                    onValueChange.invoke(newValue)
                },
                isError = isError,
                supportingText = supportingText,
                label = {
                    Text(
                        label,
                        style = TextStyle(fontSize = 14.sp, fontFamily = ariaFaNumFontFamily),
                        fontWeight = FontWeight.Medium
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                visualTransformation = AmountVisualTransformation,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = ariaFaNumFontFamily,
                    fontWeight = FontWeight.Medium
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Text(
                        text = unit,
                        modifier = Modifier
                            .padding(horizontal = 12.dp),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = ariaFaNumFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            )
        }
    }
}

private object AmountVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = text.text.formatAmountForDisplay()
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var originalIndex = 0
                var transformedIndex = 0
                while (originalIndex < offset && transformedIndex < formatted.length) {
                    if (formatted[transformedIndex] != '٬') originalIndex++
                    transformedIndex++
                }
                return transformedIndex
            }

            override fun transformedToOriginal(offset: Int): Int =
                formatted.take(offset).count { it != '٬' }.coerceAtMost(text.length)
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

@Preview
@Composable
fun AmountTextFieldPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AmountTextField(amount = TextFieldValue(text = "12,450"), unit = "تومان") {

        }
    }

}
