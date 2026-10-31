package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.Focus
import com.wander.mono.ui.components.AppBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest

/** Under the focused favourite: until when the focus lasts, and "End focus". */
data class FocusRow(val app: AppKey, val until: Long) : HomeRow {
    override val key: String get() = "focus:${app.encode()}"
}

/**
 * The [stored] focus while it lasts, null once it ends: one delay to its end (never a timer), then
 * [end] clears it. Home stops collecting while away, so an end passed during sleep is caught, and
 * cleared, as soon as it comes back.
 */
internal fun activeFocus(stored: Flow<Focus?>, end: suspend () -> Unit): Flow<Focus?> =
    stored.transformLatest { focus ->
        val left = focus?.let { it.until - System.currentTimeMillis() } ?: 0L
        if (focus != null && left > 0) {
            emit(focus)
            delay(left)
        }
        emit(null)
        if (focus != null) end()
    }.distinctUntilChanged()

/**
 * Home focused on one favourite: every other favourite and folder loses its badge, and the
 * focused one gets its [FocusRow] right under it. Without [focus], or with the favourite not on
 * home, the rows are left as they are.
 */
internal fun withFocus(rows: List<HomeRow>, focus: Focus?): List<HomeRow> {
    if (focus == null || rows.none { it is HomeRow.App && it.app.key == focus.app }) return rows
    return rows.flatMap { row ->
        when {
            row is HomeRow.App && row.app.key == focus.app -> listOf(row, FocusRow(focus.app, focus.until))
            row is HomeRow.App -> listOf(row.copy(badge = AppBadge.None))
            else -> listOf(row)
        }
    }
}

/** The focused favourite, when home shows one. */
internal fun focusedApp(rows: List<HomeRow>?): AppKey? = rows?.firstNotNullOfOrNull { (it as? FocusRow)?.app }

/** Whether a focus on [focused] sets [row] back: anything not of the focused favourite. */
internal fun dimmedByFocus(row: HomeRow, focused: AppKey?): Boolean = when {
    focused == null -> false
    row is HomeRow.App -> row.app.key != focused
    row is HomeRow.Folder -> row.apps.none { it.app.key == focused }
    row is FocusRow -> false
    row is NotificationRow -> row.app.key != focused
    row is LiveActivityRow -> row.app.key != focused
    else -> true
}
