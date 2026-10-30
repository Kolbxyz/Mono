package com.wander.mono.data

import com.wander.mono.core.apps.AppKey

/**
 * Favourites grouped under one [name] on home, in their own order. [id] stays the same through a
 * rename, so an open folder stays open. An app is in one folder at most.
 */
data class HomeFolder(val id: String, val name: String, val apps: List<AppKey>) {
    /** One line: id, name and app keys, tab-separated (names are cleaned of tabs and line breaks). */
    fun encode(): String = (listOf(id, cleanFolderName(name)) + apps.map { it.encode() }).joinToString(FIELD)

    companion object {
        private const val FIELD = "\t"

        /** Null when the id or name is missing or an app key is damaged. */
        fun decode(encoded: String): HomeFolder? {
            val fields = encoded.split(FIELD)
            val id = fields[0]
            val name = fields.getOrNull(1).orEmpty()
            val apps = fields.drop(2).map(AppKey::decode)
            val whole = id.isNotBlank() && name.isNotBlank() && null !in apps
            return if (whole) HomeFolder(id, name, apps.filterNotNull().distinct()) else null
        }
    }
}

/** A folder name as stored: on one line, without runs of spaces or surrounding blanks. */
fun cleanFolderName(name: String): String = name.replace(WHITESPACE, " ").trim()

private val WHITESPACE = Regex("\\s+")

/** [app] put at the end of the folder [id], taken out of any other; folders left empty go. */
internal fun List<HomeFolder>.withApp(id: String, app: AppKey): List<HomeFolder> = mapNotNull { folder ->
    when {
        folder.id == id -> folder.copy(apps = folder.apps - app + app)
        app in folder.apps -> folder.copy(apps = folder.apps - app).takeIf { it.apps.isNotEmpty() }
        else -> folder
    }
}

/** [app] taken out of its folder; a folder left empty goes. */
internal fun List<HomeFolder>.withoutApp(app: AppKey): List<HomeFolder> = mapNotNull { folder ->
    if (app in folder.apps) folder.copy(apps = folder.apps - app).takeIf { it.apps.isNotEmpty() } else folder
}
