package com.wander.mono.data.backup

import com.wander.mono.data.HomeFolder
import com.wander.mono.data.NowPlayingAnchor
import com.wander.mono.data.Routine
import com.wander.mono.data.SwipeAction
import com.wander.mono.data.cleanFolderName

/**
 * [HomeStructure]'s fields of the backup file. Each is optional, since files written before they
 * existed lack them: a missing field restores nothing, but one that is there and damaged makes the
 * whole file malformed, as any other field does.
 */
internal object StructureFormat {
    fun encode(structure: HomeStructure): Map<String, Any> = buildMap<String, Any> {
        put(
            FOLDERS,
            structure.folders.map { folder ->
                mapOf("id" to folder.id, "name" to folder.name, "apps" to folder.apps.map { it.encode() })
            },
        )
        put(SWIPES, structure.swipes.entries.associate { (key, action) -> key.encode() to action.encode() })
        put(MUTED, structure.mutedApps.map { it.encode() })
        put(LIMITS, structure.dailyLimits.entries.associate { (key, minutes) -> key.encode() to minutes })
        put(
            ROUTINES,
            structure.routines.map { routine ->
                mapOf("start" to routine.start, "end" to routine.end, "apps" to routine.apps.map { it.encode() })
            },
        )
        // Left out while the card follows its app, as in a file from before it could be placed.
        structure.nowPlaying?.let { put(NOW_PLAYING, it.encode()) }
    }

    fun read(tree: Map<String, Any?>): HomeStructure = HomeStructure(
        folders = tree.optionalField<List<*>>(FOLDERS, emptyList<Any?>()).map(::folder),
        swipes = tree.optionalField<Map<*, *>>(SWIPES, emptyMap<Any?, Any?>()).entries.associate { (key, action) ->
            appKey(key) to (SwipeAction.decode(string(action)) ?: throw MalformedBackup())
        },
        mutedApps = tree.optionalField<List<*>>(MUTED, emptyList<Any?>()).mapTo(mutableSetOf(), ::appKey),
        dailyLimits = tree.optionalField<Map<*, *>>(LIMITS, emptyMap<Any?, Any?>()).entries
            .associate { (key, minutes) -> appKey(key) to minutes(minutes) },
        routines = tree.optionalField<List<*>>(ROUTINES, emptyList<Any?>()).map(::routine).take(Routine.MAX),
        nowPlaying = tree[NOW_PLAYING]?.let { NowPlayingAnchor.decode(string(it)) ?: throw MalformedBackup() },
    )

    private fun folder(value: Any?): HomeFolder {
        val folder = value as? Map<*, *> ?: throw MalformedBackup()
        val name = cleanFolderName(string(folder["name"]))
        val id = string(folder["id"])
        if (id.isBlank() || name.isEmpty()) throw MalformedBackup()
        return HomeFolder(id, name, folder.strings("apps").map(::appKey).distinct())
    }

    private fun routine(value: Any?): Routine {
        val routine = value as? Map<*, *> ?: throw MalformedBackup()
        return Routine(
            start = minuteOfDay(routine["start"]),
            end = minuteOfDay(routine["end"]),
            apps = routine.strings("apps").mapTo(mutableSetOf(), ::appKey),
        )
    }

    private fun minutes(value: Any?): Int =
        integer(value)?.takeIf { it in 1..MAX_LIMIT }?.toInt() ?: throw MalformedBackup()

    private fun minuteOfDay(value: Any?): Int =
        integer(value)?.takeIf { it in 0 until MAX_LIMIT }?.toInt() ?: throw MalformedBackup()

    private const val FOLDERS = "folders"
    private const val SWIPES = "swipeLeft"
    private const val MUTED = "mutedOnHome"
    private const val LIMITS = "dailyLimits"
    private const val ROUTINES = "routines"
    private const val NOW_PLAYING = "nowPlayingAnchor"

    /** A day's worth of minutes: no limit can be longer, and no time of day later. */
    private const val MAX_LIMIT = 24L * 60
}
