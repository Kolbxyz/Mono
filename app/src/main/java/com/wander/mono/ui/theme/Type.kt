package com.wander.mono.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.wander.mono.R
import com.wander.mono.data.settings.ClockFont

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

private val weights = listOf(
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
    FontWeight.ExtraBold,
    FontWeight.Black,
)

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

/**
 * Rounded, heavier titles over a plain body, the scale Wanda and Phalanx share. Each style's
 * Expressive `…Emphasized` twin is the same size one weight up, in the same family: left unset,
 * Material would fill them from its own default typeface, and an emphasized header would be the
 * one line on screen not in Google Sans Flex.
 */
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
    displayLargeEmphasized = title(FontWeight.Black, 54.sp, 60.sp, (-1).sp),
    displayMediumEmphasized = title(FontWeight.Black, 44.sp, 52.sp, (-0.5).sp),
    displaySmallEmphasized = title(FontWeight.ExtraBold, 36.sp, 44.sp),
    headlineLargeEmphasized = title(FontWeight.ExtraBold, 32.sp, 40.sp, (-0.5).sp),
    headlineMediumEmphasized = title(FontWeight.ExtraBold, 26.sp, 34.sp, (-0.2).sp),
    headlineSmallEmphasized = title(FontWeight.ExtraBold, 22.sp, 28.sp),
    titleLargeEmphasized = title(FontWeight.Bold, 20.sp, 26.sp),
    titleMediumEmphasized = title(FontWeight.Bold, 16.sp, 22.sp, 0.1.sp),
    titleSmallEmphasized = title(FontWeight.SemiBold, 14.sp, 20.sp, 0.1.sp),
    bodyLargeEmphasized = body(FontWeight.Medium, 16.sp, 24.sp, 0.3.sp),
    bodyMediumEmphasized = body(FontWeight.Medium, 14.sp, 20.sp, 0.2.sp),
    bodySmallEmphasized = body(FontWeight.Medium, 12.sp, 16.sp),
    labelLargeEmphasized = body(FontWeight.Bold, 14.sp, 20.sp, 0.1.sp),
    labelMediumEmphasized = body(FontWeight.SemiBold, 12.sp, 16.sp, 0.4.sp),
    labelSmallEmphasized = body(FontWeight.Bold, 10.sp, 14.sp, 0.5.sp),
)

/**
 * The home clock in the user's [font], at [weight] on its `wght` axis (clamped to what the font
 * has; a static font ignores it) and [size]. Tight tracking for the proportional faces and tabular
 * figures, so the minutes do not shift the line every tick.
 */
internal fun clockStyle(font: ClockFont, weight: Int, size: TextUnit): TextStyle {
    val tracking = if (font == ClockFont.Mono) 0f else size.value * CLOCK_TRACKING
    val axis = weight.coerceIn(font.weights)
    return TextStyle(
        fontFamily = clockFamily(font, axis),
        lineHeightStyle = FullLineHeight,
        // The same weight the family's one font is declared at, so Compose never fakes a bold.
        fontWeight = FontWeight(axis),
        fontSynthesis = FontSynthesis.None,
        fontSize = size,
        lineHeight = (size.value * CLOCK_LINE).sp,
        letterSpacing = tracking.sp,
        fontFeatureSettings = "tnum",
    )
}

/** One family per face and weight, built once: a new family each tick would re-resolve the font. */
private val clockFamilies = HashMap<Pair<ClockFont, Int>, FontFamily>()

private fun clockFamily(font: ClockFont, weight: Int): FontFamily = clockFamilies.getOrPut(font to weight) {
    val resId = when (font) {
        ClockFont.Flex -> R.font.google_sans_flex
        ClockFont.Rounded -> R.font.nunito
        ClockFont.Serif -> R.font.dm_serif_display
        ClockFont.Mono -> R.font.jetbrains_mono
        ClockFont.Condensed -> R.font.oswald
    }
    val settings = when {
        font == ClockFont.Flex -> FontVariation.Settings(
            FontVariation.weight(weight),
            FontVariation.Setting("ROND", TITLE_ROUNDNESS),
        )
        font.isVariable -> FontVariation.Settings(FontVariation.weight(weight))
        else -> FontVariation.Settings()
    }
    FontFamily(Font(resId, weight = FontWeight(weight), variationSettings = settings))
}

/** Line height and tracking relative to the clock's size, as the original 88/92 sp, -2 sp face. */
private const val CLOCK_LINE = 1.05f
private const val CLOCK_TRACKING = -0.023f
