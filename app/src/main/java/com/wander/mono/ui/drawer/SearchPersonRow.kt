package com.wander.mono.ui.drawer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.contacts.ContactReach
import com.wander.mono.core.contacts.reachOf
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcons

/**
 * A contact: tapping opens it in the contacts app. Under its name, tonal chips call, text or mail
 * it, each only when the contact has a number or an address for it, and only when [onReach] is
 * given. [chipColor] stands out from what the row sits on.
 */
@Composable
fun PersonRow(
    hit: SearchHit.Person,
    icons: AppIcons,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    onReach: ((ContactReach) -> Unit)? = null,
    chipColor: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    val ways = if (onReach == null) emptyList() else reachOf(hit.contact)
    Column(modifier) {
        SymbolRow(
            symbol = R.drawable.sym_person,
            title = hit.label,
            subtitle = stringResource(R.string.search_contact),
            clickLabel = R.string.action_open,
            icons = icons,
            onClick = onOpen,
            matched = hit.matched,
        )
        if (onReach != null && ways.isNotEmpty()) {
            // Lined up with the name above: past the symbol, when there is one.
            val start = if (icons.mode == IconMode.TextOnly) 16.dp else 72.dp
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = start, end = 16.dp, bottom = 8.dp),
            ) {
                ways.forEach { how ->
                    val (label, symbol) = chipOf(how)
                    TonalChip(label, symbol, hit.label, chipColor) { onReach(how) }
                }
            }
        }
    }
}

/** A compact tonal action under a result, read out with the name it acts on ("Call Léa"). */
@Composable
private fun TonalChip(
    @StringRes label: Int,
    @DrawableRes symbol: Int,
    name: String,
    container: Color,
    onClick: () -> Unit,
) {
    val spoken = stringResource(R.string.search_reach_spoken, stringResource(label), name)
    val content = contentColorFor(container)
    AssistChip(
        onClick = onClick,
        label = { Text(stringResource(label)) },
        leadingIcon = { Icon(painterResource(symbol), null, Modifier.size(AssistChipDefaults.IconSize)) },
        shape = CircleShape,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = container,
            labelColor = content,
            leadingIconContentColor = content,
        ),
        border = null,
        modifier = Modifier.semantics { contentDescription = spoken },
    )
}

private fun chipOf(how: ContactReach): Pair<Int, Int> = when (how) {
    ContactReach.Call -> R.string.search_reach_call to R.drawable.sym_call
    ContactReach.Message -> R.string.search_reach_message to R.drawable.sym_chat
    ContactReach.Email -> R.string.search_reach_email to R.drawable.sym_mail
}
