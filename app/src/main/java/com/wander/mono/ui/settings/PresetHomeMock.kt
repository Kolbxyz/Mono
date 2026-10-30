package com.wander.mono.ui.settings

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wander.mono.data.settings.ClockStyle
import com.wander.mono.data.settings.IconMode
import com.wander.mono.data.settings.PresetLook
import com.wander.mono.data.settings.RowDensity
import com.wander.mono.ui.theme.clockStyle
import com.wander.mono.ui.theme.toMask
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Home in miniature as [look] would draw it in [scheme]: the clock in its face and style, then
 * three app rows with their icons in its shape and mode (labels drawn as bars, so no app is made
 * up), at its density. With the text shadow there is no scrim, as on home.
 */
@Composable
internal fun PresetHomeMock(look: PresetLook, scheme: ColorScheme, modifier: Modifier = Modifier) {
    val shadow = if (look.textShadow) Shadow(scheme.surface, blurRadius = SHADOW_BLUR) else null
    Column(modifier, verticalArrangement = Arrangement.spacedBy(rowGap(look.density))) {
        Text(
            rememberMockTime(stacked = look.clock.style == ClockStyle.Stacked),
            style = clockStyle(look.clock.font, look.clock.weight, clockSize(look.clock.style))
                .copy(color = scheme.onSurface, shadow = shadow),
        )
        Spacer(Modifier.height(8.dp))
        LabelWidths.forEachIndexed { index, width ->
            MockRow(look, scheme, accent = accentOf(scheme, index), labelWidth = width)
        }
    }
}

@Composable
private fun MockRow(look: PresetLook, scheme: ColorScheme, accent: Color, labelWidth: Float) {
    val iconShape = look.iconShape.toMask() ?: CircleShape
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when (look.iconMode) {
            IconMode.Colour -> Box(Modifier.size(iconSize(look.density)).background(accent, iconShape))
            IconMode.Themed -> Box(
                Modifier.size(iconSize(look.density)).background(scheme.surfaceContainerHighest, iconShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(iconSize(look.density) / 2).background(scheme.onSurfaceVariant, CircleShape))
            }
            IconMode.TextOnly -> Unit
        }
        Box(
            Modifier
                .fillMaxWidth(labelWidth)
                .height(LabelHeight)
                .background(scheme.onSurface.copy(alpha = LABEL_ALPHA), CircleShape),
        )
    }
}

/** The time now in the user's 12 or 24 hour format, hours over minutes when [stacked]; not ticking. */
@Composable
private fun rememberMockTime(stacked: Boolean): String {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    return remember(stacked, locale) {
        val is24Hour = DateFormat.is24HourFormat(context)
        val now = LocalTime.now()
        val format = { pattern: String -> now.format(DateTimeFormatter.ofPattern(pattern, locale)) }
        if (stacked) {
            format(if (is24Hour) "HH" else "hh") + "\n" + format("mm")
        } else {
            // Without the AM/PM marker, as home's clock.
            format(DateFormat.getBestDateTimePattern(locale, if (is24Hour) "Hm" else "hm").replace("a", "").trim())
        }
    }
}

private fun accentOf(scheme: ColorScheme, index: Int): Color = when (index % ACCENTS) {
    0 -> scheme.primaryContainer
    1 -> scheme.secondaryContainer
    else -> scheme.tertiaryContainer
}

private fun clockSize(style: ClockStyle): TextUnit = when (style) {
    ClockStyle.Default -> 34.sp
    ClockStyle.Stacked -> 28.sp
    ClockStyle.Small -> 20.sp
}

private fun iconSize(density: RowDensity): Dp = when (density) {
    RowDensity.Compact -> 14.dp
    RowDensity.Default -> 16.dp
    RowDensity.Comfortable -> 20.dp
}

private fun rowGap(density: RowDensity): Dp = when (density) {
    RowDensity.Compact -> 6.dp
    RowDensity.Default -> 9.dp
    RowDensity.Comfortable -> 12.dp
}

/** The three rows' label lengths, as fractions of the row left after the icon. */
private val LabelWidths = listOf(LABEL_LONG, LABEL_SHORT, LABEL_MIDDLE)
private const val LABEL_LONG = 0.7f
private const val LABEL_SHORT = 0.5f
private const val LABEL_MIDDLE = 0.6f
private val LabelHeight = 6.dp
private const val LABEL_ALPHA = 0.8f
private const val ACCENTS = 3
private const val SHADOW_BLUR = 8f
