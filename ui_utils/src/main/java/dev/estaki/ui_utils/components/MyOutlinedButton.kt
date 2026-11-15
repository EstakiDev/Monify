package dev.estaki.ui_utils.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.estaki.ui_utils.ui.theme.ariaFaNumFontFamily

@Composable
fun MyOutlinedButton(
    modifier: Modifier = Modifier,
    text: String,
    shape: RoundedCornerShape = RoundedCornerShape(20),
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary,
        containerColor = MaterialTheme.colorScheme.background
    ),
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    onBtnClicked: () -> Unit
) {
    OutlinedButton(
        modifier = modifier
            .fillMaxWidth(),
        onClick = { onBtnClicked.invoke() },
        border = border,
        shape = shape, // = 20% percent
        // or shape = CircleShape
        colors = colors,

    ) {
        Text(
            modifier = Modifier.padding(vertical = 6.dp),
            text = text,
            fontFamily = ariaFaNumFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview
@Composable
fun MyOutlinedButtonPreview() {
    MyOutlinedButton(text = "تایید دسترسی") { }
}
