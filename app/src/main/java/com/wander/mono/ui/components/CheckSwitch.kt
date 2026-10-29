package com.wander.mono.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.wander.mono.R

/**
 * A switch for a row that toggles as a whole (so the switch itself takes no input), with a check
 * in its thumb while on, the Expressive cue Wanda and Phalanx use too.
 */
@Composable
fun CheckSwitch(checked: Boolean, modifier: Modifier = Modifier) {
    Switch(
        checked = checked,
        onCheckedChange = null,
        modifier = modifier,
        thumbContent = if (checked) {
            { Icon(painterResource(R.drawable.sym_check), null, Modifier.size(SwitchDefaults.IconSize)) }
        } else {
            null
        },
    )
}
