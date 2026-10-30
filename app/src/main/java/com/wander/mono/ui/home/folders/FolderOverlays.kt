package com.wander.mono.ui.home.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wander.mono.R
import com.wander.mono.data.HomeFolder
import com.wander.mono.ui.components.ActionButtonGroup
import com.wander.mono.ui.components.ActionEmphasis
import com.wander.mono.ui.components.MenuAction
import com.wander.mono.ui.components.MonoSheet
import com.wander.mono.ui.sheet.SheetHeader
import com.wander.mono.ui.sheet.afterDismiss
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * What opens over home for folders: one folder's options (rename, ungroup) and the name dialog,
 * for a rename or for a new folder asked for from an app's options. Windows of their own, so
 * where this sits in the layout does not matter.
 */
@Composable
internal fun FolderOverlays(folders: FolderViewModel) {
    val options by folders.options.collectAsStateWithLifecycle()
    val naming by folders.naming.collectAsStateWithLifecycle()
    options?.let { folder -> FolderSheet(folder, folders) }
    naming?.let { purpose ->
        FolderNameDialog(purpose, onSave = { folders.saveName(purpose, it) }, onDismiss = folders::dismissNaming)
    }
}

/** A folder's long-press options. Both actions run once the sheet has slid away. */
@Composable
private fun FolderSheet(folder: HomeFolder, folders: FolderViewModel) {
    MonoSheet(onDismissRequest = { folders.showOptions(null) }) { dismiss ->
        SheetHeader(
            title = folder.name,
            leading = {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, folderShape()),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(R.drawable.sym_folder),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            },
            supporting = {
                Text(
                    pluralStringResource(R.plurals.folder_app_count, folder.apps.size, folder.apps.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryTextColor(),
                )
            },
        )
        ActionButtonGroup(
            listOf(
                MenuAction(R.drawable.sym_edit, R.string.folder_rename, ActionEmphasis.Primary) {
                    folders.startRename(folder)
                }.afterDismiss(dismiss),
                MenuAction(R.drawable.sym_folder, R.string.folder_ungroup, ActionEmphasis.Danger) {
                    folders.dissolve(folder.id)
                }.afterDismiss(dismiss),
            ),
            Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
    }
}
