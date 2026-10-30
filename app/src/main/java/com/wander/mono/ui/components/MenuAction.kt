package com.wander.mono.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

/** How loudly an action is drawn, and so where [ActionButtonGroup] places it. */
enum class ActionEmphasis {
    /** The reason the menu was opened. Big, filled, first row. */
    Primary,

    /** First row too, but second to [Primary]. */
    Secondary,

    /** Everything else with a label. */
    Tonal,

    /** Reads from its icon alone. Grouped four to a short row. */
    Icon,

    /** Removes something. Always last, full width, in the error colour; [IconActionRows] tints it. */
    Danger,
}

/**
 * One button of a context menu. [selected] is non-null for an on/off setting: drawn as a rounded
 * square, outlined while off and filled while on. [labelArg] fills a `%s` in [label].
 * [IconActionRows] reads [group], the row the action sits in (rows in ascending order), and
 * [shortLabel], the word under its icon, [label] then being what TalkBack says.
 */
@Immutable
class MenuAction(
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
    val emphasis: ActionEmphasis = ActionEmphasis.Tonal,
    val selected: Boolean? = null,
    val labelArg: String? = null,
    val group: Int = 0,
    @StringRes val shortLabel: Int? = null,
    val onClick: () -> Unit,
)
