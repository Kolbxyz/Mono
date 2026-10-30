package com.wander.mono.ui.home.folders

import com.wander.mono.data.HomeFolder
import com.wander.mono.ui.components.AppBadge
import com.wander.mono.ui.home.HomeRow

/**
 * Groups the assembled home [rows] into [folders]: each folder takes the place of its first shown
 * favourite and holds its shown favourites in the folder's own order. The live rows of those
 * favourites (now playing, preview, calendar, widgets) follow the folder, so a closed folder
 * never hides them. A folder with no favourite shown (quiet mode, all uninstalled) is left out.
 */
internal fun withFolders(rows: List<HomeRow>, folders: List<HomeFolder>): List<HomeRow> {
    if (folders.isEmpty()) return rows
    val folderOf = folders.flatMap { folder -> folder.apps.map { it to folder } }.toMap()
    val members = LinkedHashMap<HomeFolder, MutableList<HomeRow.App>>()
    val live = mutableMapOf<HomeFolder, MutableList<HomeRow>>()
    val slots = mutableListOf<Slot>()
    // The folder of the favourite above, while its live rows follow it.
    var owner: HomeFolder? = null
    for (row in rows) {
        val folder = (row as? HomeRow.App)?.let { folderOf[it.app.key] }
        when {
            row is HomeRow.App && folder != null -> {
                if (folder !in members) slots += Slot.Folder(folder)
                members.getOrPut(folder) { mutableListOf() } += row
                owner = folder
            }
            row is HomeRow.App || row is HomeRow.ListenerPrompt -> {
                owner = null
                slots += Slot.Row(row)
            }
            else -> {
                val above = owner
                if (above != null) live.getOrPut(above) { mutableListOf() } += row else slots += Slot.Row(row)
            }
        }
    }
    return slots.flatMap { slot ->
        when (slot) {
            is Slot.Row -> listOf(slot.row)
            is Slot.Folder ->
                listOf(folderRow(slot.folder, members.getValue(slot.folder))) + live[slot.folder].orEmpty()
        }
    }
}

/** Every folder replaced by its favourites, as edit mode shows and saves home. */
internal fun flattenFolders(rows: List<HomeRow>): List<HomeRow> =
    rows.flatMap { if (it is HomeRow.Folder) it.apps else listOf(it) }

/** The [open] folders followed by their favourites, as home shows them; closed ones alone. */
internal fun openFolders(rows: List<HomeRow>, open: Set<String>): List<HomeRow> =
    rows.flatMap { if (it is HomeRow.Folder && it.id in open) listOf(it) + it.apps else listOf(it) }

/** The keys of the favourites [openFolders] shows inside a folder, drawn indented. */
internal fun folderMemberKeys(rows: List<HomeRow>, open: Set<String>): Set<String> = rows
    .filter { it is HomeRow.Folder && it.id in open }
    .flatMapTo(mutableSetOf()) { folder -> (folder as HomeRow.Folder).apps.map { it.key } }

/** Counts add up; with dots, one is enough to mark the folder. */
internal fun folderBadge(apps: List<HomeRow.App>): AppBadge {
    val count = apps.sumOf { (it.badge as? AppBadge.Count)?.count ?: 0 }
    return when {
        count > 0 -> AppBadge.Count(count)
        apps.any { it.badge == AppBadge.Dot } -> AppBadge.Dot
        else -> AppBadge.None
    }
}

private fun folderRow(folder: HomeFolder, shown: List<HomeRow.App>): HomeRow.Folder {
    val apps = shown.sortedBy { folder.apps.indexOf(it.app.key) }
    return HomeRow.Folder(folder.id, folder.name, apps, folderBadge(apps))
}

/** A place in the grouped list: a row as it was, or where a folder goes. */
private sealed interface Slot {
    class Row(val row: HomeRow) : Slot

    class Folder(val folder: HomeFolder) : Slot
}
