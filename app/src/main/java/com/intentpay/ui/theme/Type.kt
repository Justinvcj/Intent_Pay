package com.intentpay.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Apple's text styles mapped onto Material3 slots. Body is 17 and nothing
// readable drops below 11. Weights stay 400 / 500 / 600, tracking stays at 0.
private val Sans = FontFamily.SansSerif

private fun style(size: Int, line: Int, weight: FontWeight, tabular: Boolean = false) = TextStyle(
    fontFamily = Sans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = if (tabular) "tnum" else null
)

val Typography = Typography(
    // Figures: the monthly total and per-category amounts.
    displayMedium = style(44, 48, FontWeight.SemiBold, tabular = true),
    displaySmall = style(34, 40, FontWeight.SemiBold, tabular = true),
    // Large title for the four tab screens.
    headlineLarge = style(34, 41, FontWeight.SemiBold),
    headlineMedium = style(28, 34, FontWeight.SemiBold),
    headlineSmall = style(22, 28, FontWeight.SemiBold),
    titleLarge = style(22, 28, FontWeight.SemiBold),
    titleMedium = style(17, 22, FontWeight.SemiBold),
    titleSmall = style(15, 20, FontWeight.SemiBold),
    bodyLarge = style(17, 22, FontWeight.Normal),
    bodyMedium = style(15, 20, FontWeight.Normal),
    bodySmall = style(13, 18, FontWeight.Normal),
    labelLarge = style(17, 22, FontWeight.Medium),
    labelMedium = style(13, 18, FontWeight.Medium),
    labelSmall = style(11, 13, FontWeight.Medium)
)
