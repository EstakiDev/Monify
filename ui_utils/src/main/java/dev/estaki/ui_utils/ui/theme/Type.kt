package dev.estaki.ui_utils.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.estaki.ui_utils.R


// Set of Material typography styles to start with
val iranSansFontFamily = FontFamily(
    Font(R.font.iran_sans_mobile_ultra_light, FontWeight.ExtraLight),
    Font(R.font.iran_sans_mobile_light, FontWeight.Light),
    Font(R.font.iran_sans_mobile, FontWeight.Normal),
    Font(R.font.iran_sans_mobile_medium, FontWeight.Medium),
    Font(R.font.iran_sans_mobile_bold, FontWeight.Bold),
    Font(R.font.iran_sans_mobile_black, FontWeight.Black),
)

val iranSansFaNumFontFamily = FontFamily(
    Font(R.font.iran_sans_mobile_fa_num_ultra_light, FontWeight.ExtraLight),
    Font(R.font.iran_sans_mobile_fa_num_light, FontWeight.Light),
    Font(R.font.iran_sans_mobile_fa_num, FontWeight.Normal),
    Font(R.font.iran_sans_mobile_fa_num_medium, FontWeight.Medium),
    Font(R.font.iran_sans_mobile_fa_num_bold, FontWeight.Bold),
    Font(R.font.iran_sans_mobile_fa_num_black, FontWeight.Black),
)
val vazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_black, FontWeight.Black),
)

val ariaFaNumFontFamily = vazirmatnFontFamily
val coolakFaNumFontFamily = vazirmatnFontFamily

private fun Typography.withVazirmatn() = copy(
    displayLarge = displayLarge.copy(fontFamily = vazirmatnFontFamily),
    displayMedium = displayMedium.copy(fontFamily = vazirmatnFontFamily),
    displaySmall = displaySmall.copy(fontFamily = vazirmatnFontFamily),
    headlineLarge = headlineLarge.copy(fontFamily = vazirmatnFontFamily),
    headlineMedium = headlineMedium.copy(fontFamily = vazirmatnFontFamily),
    headlineSmall = headlineSmall.copy(fontFamily = vazirmatnFontFamily),
    titleLarge = titleLarge.copy(fontFamily = vazirmatnFontFamily),
    titleMedium = titleMedium.copy(fontFamily = vazirmatnFontFamily),
    titleSmall = titleSmall.copy(fontFamily = vazirmatnFontFamily),
    labelLarge = labelLarge.copy(fontFamily = vazirmatnFontFamily),
    labelMedium = labelMedium.copy(fontFamily = vazirmatnFontFamily),
    labelSmall = labelSmall.copy(fontFamily = vazirmatnFontFamily),
    bodyLarge = bodyLarge.copy(fontFamily = vazirmatnFontFamily),
    bodyMedium = bodyMedium.copy(fontFamily = vazirmatnFontFamily),
    bodySmall = bodySmall.copy(fontFamily = vazirmatnFontFamily)
)

val TypographyDark = Typography(

    bodySmall = TextStyle(
        fontFamily = ariaFaNumFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = ColorTextGrayOnDarkTheme,
    ),
    bodyMedium = TextStyle(
        fontFamily = ariaFaNumFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
        color = ColorTextGrayOnDarkTheme,
    ),
    bodyLarge = TextStyle(
        fontFamily = ariaFaNumFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
        color = ColorTextGrayOnDarkTheme,
    ),
).withVazirmatn()

val TypographyLite = Typography(
    bodySmall =  TextStyle(
        fontFamily = ariaFaNumFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = ColorTextGrayOnLiteTheme,
    ),
    bodyMedium = TextStyle(
        fontFamily = ariaFaNumFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
        color = ColorTextGrayOnLiteTheme,
    ),
    bodyLarge = TextStyle(
        fontFamily = ariaFaNumFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
        color = ColorTextGrayOnLiteTheme,
    ),
).withVazirmatn()

/* Other default text styles to override
titleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
),
labelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)
*/
