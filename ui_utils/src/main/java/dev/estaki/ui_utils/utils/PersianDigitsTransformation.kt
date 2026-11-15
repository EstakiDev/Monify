package dev.estaki.ui_utils.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import dev.estaki.kt_pure_utils.toPersianDigits

object PersianDigitsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(AnnotatedString(text.text.toPersianDigits()), OffsetMapping.Identity)
}
