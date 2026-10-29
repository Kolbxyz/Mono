package com.wander.mono.core.widgets

import com.wander.mono.core.apps.AppKey

/**
 * A hosted widget on home: its bound id, the provider (a flattened `ComponentName`) and its profile
 * serial, the favourite it sits under ([anchor], null for above the first favourite) and its height.
 */
data class WidgetPlacement(
    val appWidgetId: Int,
    val provider: String,
    val userSerial: Long,
    val anchor: AppKey?,
    val heightDp: Int,
) {
    fun encode(): String =
        listOf(appWidgetId, provider, userSerial, anchor?.encode().orEmpty(), heightDp).joinToString(SEPARATOR)

    companion object {
        // Neither a flattened component name nor an encoded AppKey contains a bar.
        private const val SEPARATOR = "|"
        private const val PARTS = 5

        fun decode(encoded: String): WidgetPlacement? {
            val parts = encoded.split(SEPARATOR).takeIf { it.size == PARTS } ?: return null
            val (id, provider, serial) = parts
            val (anchor, height) = parts.takeLast(2)
            // An anchor that is present must decode; an empty one means "above the first favourite".
            val anchorKey = anchor.takeIf { it.isNotEmpty() }?.let(AppKey::decode)
            val valid = provider.isNotEmpty() && (anchor.isEmpty() || anchorKey != null)
            return id.toIntOrNull()?.let { appWidgetId ->
                val userSerial = serial.toLongOrNull()
                val heightDp = height.toIntOrNull()
                if (valid && userSerial != null && heightDp != null) {
                    WidgetPlacement(appWidgetId, provider, userSerial, anchorKey, heightDp)
                } else {
                    null
                }
            }
        }
    }
}

/** The three heights a widget row can take; each is roughly one more launcher cell. */
enum class WidgetSize(val heightDp: Int) {
    Small(SMALL_DP),
    Medium(MEDIUM_DP),
    Large(LARGE_DP),
    ;

    companion object {
        /** The smallest size that fits the provider's minimum height, or the largest when none does. */
        fun fitting(minHeightDp: Int): WidgetSize = entries.firstOrNull { it.heightDp >= minHeightDp } ?: Large

        /** The size nearest to a stored height. */
        fun nearest(heightDp: Int): WidgetSize = entries.minBy { kotlin.math.abs(it.heightDp - heightDp) }
    }
}

private const val SMALL_DP = 112
private const val MEDIUM_DP = 184
private const val LARGE_DP = 280
