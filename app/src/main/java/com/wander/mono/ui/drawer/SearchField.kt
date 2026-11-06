package com.wander.mono.ui.drawer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.data.RecentSearch
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.rememberMonoHaptics

/**
 * The drawer's search box, with Mono's settings at its end. The keyboard's action key opens the top
 * result. [focusRequester] lets the drawer focus it when opened from home's search bar; otherwise only
 * a tap on it brings up the keyboard. It sits at the bottom of the drawer, in the thumb's reach: while
 * it is focused and empty, the [recent] searches show above it as chips ([RecentSearchChips]).
 * [collapsed], it folds into a round search button at the bottom end, and a tap on that ([onExpand])
 * grows the field back out of it, the button widening into the pill on the scheme's spatial spring.
 * [onBlur] hears the field losing the focus it had. A long press on the button ([onLongPress]) opens
 * [menu], anchored to it.
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onOpenSettings: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    recent: List<RecentSearch> = emptyList(),
    onForgetRecent: (RecentSearch) -> Unit = {},
    collapsed: Boolean = false,
    onExpand: () -> Unit = {},
    onBlur: () -> Unit = {},
    onLongPress: () -> Unit = {},
    menu: @Composable () -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    val motion = MaterialTheme.motionScheme
    Column(modifier) {
        RecentSearchChips(focused && query.isEmpty(), recent, onUse = onQueryChange, onForget = onForgetRecent)
        AnimatedContent(
            targetState = collapsed,
            // Wrapping what it shows, at the end, the thumb's side: as the pill takes the button's
            // place the box widens from there, revealing the pill out of the button.
            modifier = Modifier.align(Alignment.End),
            contentAlignment = Alignment.BottomEnd,
            transitionSpec = {
                (fadeIn(motion.fastEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()))
                    .using(SizeTransform(clip = true) { _, _ -> motion.defaultSpatialSpec() })
            },
            label = "searchField",
        ) { folded ->
            if (folded) {
                SearchButton(onExpand, onLongPress, menu)
            } else {
                Field(query, onQueryChange, onSubmit, onOpenSettings, focusRequester) { now ->
                    if (focused && !now) onBlur()
                    focused = now
                }
            }
        }
    }
}

/**
 * The field folded away: a round search button, tonal like the pill it grows into; a tap grows the
 * field ([onExpand]), a long press opens [menu] next to it ([onLongPress]).
 */
@Composable
private fun SearchButton(onExpand: () -> Unit, onLongPress: () -> Unit, menu: @Composable () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val haptics = rememberMonoHaptics()
    val label = stringResource(R.string.drawer_search_open)
    val optionsLabel = stringResource(R.string.drawer_list_options)
    val openMenu = {
        haptics.longPress()
        onLongPress()
    }
    Box {
        Surface(
            modifier = Modifier
                .size(BUTTON_SIZE)
                .expressivePress(interaction, pressedScale = BUTTON_PRESSED_SCALE)
                .clearAndSetSemantics {
                    contentDescription = label
                    onClick { onExpand().let { true } }
                    onLongClick(optionsLabel) { openMenu().let { true } }
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Box(
                Modifier.combinedClickable(
                    interactionSource = interaction,
                    indication = ripple(),
                    onClick = onExpand,
                    onLongClick = openMenu,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.sym_search), contentDescription = null)
            }
        }
        menu()
    }
}

@Composable
private fun Field(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onOpenSettings: () -> Unit,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChange(it.isFocused) },
        singleLine = true,
        shape = CircleShape,
        // One line, as the field is: at large font sizes the hint ends in an ellipsis instead of wrapping.
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = {
            Text(stringResource(R.string.drawer_search_hint), maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingIcon = { Icon(painterResource(R.drawable.sym_search), contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onOpenSettings) {
                Icon(painterResource(R.drawable.sym_settings), stringResource(R.string.settings_title))
            }
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Go,
        ),
        keyboardActions = KeyboardActions(onGo = { onSubmit() }),
        colors = fieldColors(),
    )
}

/**
 * A floating pill rather than a box: the container lets what is behind it (wallpaper, blur or
 * surface) through at rest and firms up while typing, with no indicator line; the icons and hint
 * sit back in onSurfaceVariant, the typed text in onSurface.
 */
@Composable
private fun fieldColors() = with(MaterialTheme.colorScheme) {
    TextFieldDefaults.colors(
        focusedContainerColor = surfaceContainerHigh.copy(alpha = FOCUSED_ALPHA),
        unfocusedContainerColor = surfaceContainerHigh.copy(alpha = RESTING_ALPHA),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedLeadingIconColor = primary,
        unfocusedLeadingIconColor = onSurfaceVariant,
        focusedTrailingIconColor = onSurfaceVariant,
        unfocusedTrailingIconColor = onSurfaceVariant,
        focusedPlaceholderColor = onSurfaceVariant,
        unfocusedPlaceholderColor = onSurfaceVariant,
    )
}

private val BUTTON_SIZE = 56.dp

/** How far the round button gives under a press. */
private const val BUTTON_PRESSED_SCALE = 0.9f

/** The pill at rest: translucent enough to float over the wallpaper, solid enough to read on any. */
private const val RESTING_ALPHA = 0.82f

/** While typing the pill is nearly opaque, so results never fight the field for attention. */
private const val FOCUSED_ALPHA = 0.96f
