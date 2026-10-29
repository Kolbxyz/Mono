package com.wander.mono.ui.drawer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.apps.PrivateSpaceState
import com.wander.mono.core.apps.WorkProfile
import com.wander.mono.ui.components.AppIcons

/**
 * The private space at the very bottom of the drawer: its state, a button to lock or unlock it and,
 * on Android 16, its settings; its apps under that while it is unlocked.
 */
@Composable
internal fun PrivateSection(
    space: PrivateSpaceState.Present,
    apps: List<DrawerEntry>,
    icons: AppIcons,
    actions: PrivateSpaceActions,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileCard(
        title = R.string.drawer_private,
        symbol = if (space.locked) R.drawable.sym_lock else R.drawable.sym_lock_open,
        status = if (space.locked) R.string.drawer_private_locked else R.string.drawer_private_open,
        open = !space.locked,
        apps = apps,
        icons = icons,
        onLaunchApp = onLaunchApp,
        onLongPress = onLongPress,
        modifier = modifier,
    ) {
        if (space.canOpenSettings) {
            IconButton(onClick = actions.onOpenSettings) {
                Icon(painterResource(R.drawable.sym_settings), stringResource(R.string.drawer_private_settings))
            }
        }
        // An action, not a toggle: TalkBack reads what tapping does ("Unlock private space").
        ProfileButton(
            symbol = if (space.locked) R.drawable.sym_lock_open else R.drawable.sym_lock,
            label = if (space.locked) R.string.drawer_unlock else R.string.drawer_lock,
            description = if (space.locked) R.string.drawer_private_unlock else R.string.drawer_private_lock,
            onPress = { actions.onSetLocked(!space.locked) },
        )
    }
}

/** A work profile at the bottom of the drawer: paused or on, a button to pause or resume it, its apps while on. */
@Composable
internal fun WorkSectionCard(
    section: WorkSection,
    icons: AppIcons,
    onSetPaused: (WorkProfile, Boolean) -> Unit,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = section.profile
    ProfileCard(
        title = R.string.drawer_work,
        symbol = R.drawable.sym_work,
        status = if (profile.paused) R.string.drawer_work_paused else R.string.drawer_work_on,
        open = !profile.paused,
        apps = section.apps,
        icons = icons,
        onLaunchApp = onLaunchApp,
        onLongPress = onLongPress,
        modifier = modifier,
    ) {
        if (profile.canToggle) {
            ProfileButton(
                symbol = if (profile.paused) R.drawable.sym_play_arrow_filled else R.drawable.sym_pause_filled,
                label = if (profile.paused) R.string.drawer_work_resume else R.string.drawer_work_pause,
                description = if (profile.paused) R.string.drawer_work_resume_full else R.string.drawer_work_pause_full,
                onPress = { onSetPaused(profile, !profile.paused) },
            )
        }
    }
}

/** A short tonal button ("Unlock"); [description] says in full what it does, for TalkBack. */
@Composable
private fun ProfileButton(
    @DrawableRes symbol: Int,
    @StringRes label: Int,
    @StringRes description: Int,
    onPress: () -> Unit,
) {
    val spoken = stringResource(description)
    FilledTonalButton(
        onClick = onPress,
        shapes = ButtonDefaults.shapes(),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = spoken
            role = Role.Button
            onClick {
                onPress()
                true
            }
        },
    ) {
        Icon(painterResource(symbol), null, Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(stringResource(label))
    }
}
