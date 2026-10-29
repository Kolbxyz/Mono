package com.wander.mono.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.wander.mono.R

/*
 * One bundled variable font, Google Sans Flex (OFL), the same file Wanda and Phalanx ship, so the
 * three apps read as one family. Bundled, never fetched: the downloadable-fonts provider is a Play
 * services dependency, and it cannot dial the roundness axis per style anyway.
 */

/** How far up the font's 0–100 `ROND` axis titles sit; full roundness on bold weights eats letterforms. */
private const val TITLE_ROUNDNESS = 65f

private fun flexFont(weight: FontWeight, roundness: Float) = Font(
    resId = R.font.google_sans_flex,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.Setting("ROND", roundness),
    ),
)

private val weights =
    listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold)

private val BodyFamily = FontFamily(weights.map { flexFont(it, roundness = 0f) })

private val RoundedFamily = FontFamily(weights.map { flexFont(it, TITLE_ROUNDNESS) })

/** Centred glyphs and no trimming, so tall descenders never get clipped by a tight line box. */
private val FullLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun title(weight: FontWeight, size: TextUnit, line: TextUnit, spacing: TextUnit = 0.sp) = TextStyle(
    fontFamily = RoundedFamily,
    lineHeightStyle = FullLineHeight,
    fontWeight = weight,
    fontSize = size,
    lineHeight = line,
    letterSpacing = spacing,
)

private fun body(weight: FontWeight, size: TextUnit, line: TextUnit, spacing: TextUnit = 0.sp) = TextStyle(
    fontFamily = BodyFamily,
    lineHeightStyle = FullLineHeight,
    fontWeight = weight,
    fontSize = size,
    lineHeight = line,
    letterSpacing = spacing,
)

/** Rounded, heavier titles over a plain body, the scale Wanda and Phalanx share. */
internal val MonoTypography = Typography(
    displayLarge = title(FontWeight.ExtraBold, 54.sp, 60.sp, (-1).sp),
    displayMedium = title(FontWeight.ExtraBold, 44.sp, 52.sp, (-0.5).sp),
    displaySmall = title(FontWeight.Bold, 36.sp, 44.sp),
    headlineLarge = title(FontWeight.Bold, 32.sp, 40.sp, (-0.5).sp),
    headlineMedium = title(FontWeight.Bold, 26.sp, 34.sp, (-0.2).sp),
    headlineSmall = title(FontWeight.Bold, 22.sp, 28.sp),
    titleLarge = title(FontWeight.SemiBold, 20.sp, 26.sp),
    titleMedium = title(FontWeight.SemiBold, 16.sp, 22.sp, 0.1.sp),
    titleSmall = title(FontWeight.Medium, 14.sp, 20.sp, 0.1.sp),
    bodyLarge = body(FontWeight.Normal, 16.sp, 24.sp, 0.3.sp),
    bodyMedium = body(FontWeight.Normal, 14.sp, 20.sp, 0.2.sp),
    bodySmall = body(FontWeight.Normal, 12.sp, 16.sp),
    labelLarge = body(FontWeight.SemiBold, 14.sp, 20.sp, 0.1.sp),
    labelMedium = body(FontWeight.Medium, 12.sp, 16.sp, 0.4.sp),
    labelSmall = body(FontWeight.SemiBold, 10.sp, 14.sp, 0.5.sp),
)

/**
 * The home clock: the display face, extra-bold and tight, with tabular figures so the minutes do
 * not shift the line every tick. [compact] is the landscape size.
 */
internal fun clockStyle(compact: Boolean): TextStyle =
    title(
        FontWeight.ExtraBold,
        if (compact) CompactClockSize else ClockSize,
        if (compact) CompactClockLine else ClockLine,
        ClockTracking,
    ).copy(fontFeatureSettings = "tnum")

private val ClockSize = 88.sp
private val ClockLine = 92.sp
private val CompactClockSize = 56.sp
private val CompactClockLine = 60.sp
private val ClockTracking = (-2).sp
