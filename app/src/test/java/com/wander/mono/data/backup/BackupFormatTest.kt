package com.wander.mono.data.backup

import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.HomeFolder
import com.wander.mono.data.LiveRowKind
import com.wander.mono.data.LiveRowRef
import com.wander.mono.data.Routine
import com.wander.mono.data.SwipeAction
import com.wander.mono.data.settings.BadgeSettings
import com.wander.mono.data.settings.SettingValue
import com.wander.mono.data.settings.ThemeMode
import com.wander.mono.data.settings.ThemeSettings
import com.wander.mono.data.settings.backedUpSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupFormatTest {
    private val mail = AppKey("org.mail", "org.mail.Main", 0)
    private val work = AppKey("org.chat", "org.chat.Main", 10)

    private val backup = Backup(
        mainSerial = 0,
        favorites = listOf(work, mail),
        hidden = setOf(AppKey("org.game", "org.game.Main", 0)),
        labels = mapOf(mail to "Post"),
        liveRowsOff = setOf(LiveRowRef(mail, LiveRowKind.NotificationPreview)),
        quietApps = setOf(mail),
        headphoneApps = setOf(mail),
        settings = backedUpSettings.map { SettingValue.default(it) }
            .replace(SettingValue(ThemeSettings.Mode, ThemeMode.Dark))
            .replace(SettingValue(BadgeSettings.ShowCounts, false)),
        lockedApps = setOf(work),
        structure = HomeStructure(
            folders = listOf(HomeFolder("talk", "Talk", listOf(work, mail))),
            swipes = mapOf(mail to SwipeAction.AppInfo, work to SwipeAction.Shortcut("room|1")),
            mutedApps = setOf(work),
            dailyLimits = mapOf(mail to 30),
            routines = listOf(Routine(start = 1320, end = 60, apps = setOf(mail))),
        ),
    )

    @Test
    fun `a backup survives encoding and decoding`() {
        assertEquals(BackupRead.Read(backup), decode(BackupFormat.encode(backup)))
    }

    @Test
    fun `settings are stored by name in their stored form`() {
        @Suppress("UNCHECKED_CAST")
        val settings = BackupFormat.encode(backup)["settings"] as Map<String, Any>
        assertEquals("dark", settings["theme_mode"])
        assertEquals(false, settings["badge_show_counts"])
    }

    @Test
    fun `a setting missing from the file goes back to its default and unknown ones are ignored`() {
        val tree = BackupFormat.encode(backup).toMutableMap()
        tree["settings"] = mapOf("theme_mode" to "light", "from_the_future" to true)
        val read = decode(tree) as BackupRead.Read
        assertEquals(ThemeMode.Light, read.backup.settings.valueOf(ThemeSettings.Mode))
        assertEquals(true, read.backup.settings.valueOf(BadgeSettings.ShowCounts))
    }

    @Test
    fun `a newer version is refused whole`() {
        val tree = BackupFormat.encode(backup) + ("version" to BackupFormat.VERSION + 1)
        assertEquals(BackupRead.Failed(BackupError.Newer), decode(tree))
    }

    @Test
    fun `a file of another kind is not a backup`() {
        assertEquals(BackupRead.Failed(BackupError.NotABackup), decode(mapOf("name" to "notes")))
    }

    @Test
    fun `damaged fields make the whole file malformed`() {
        val damaged = listOf(
            "version" to "1",
            "version" to 0,
            "mainProfile" to 1.5,
            "favorites" to listOf("not a key"),
            "hidden" to "org.game/org.game.Main/0",
            "labels" to mapOf(mail.encode() to 3),
            "liveRowsOff" to listOf("weather|${mail.encode()}"),
            "settings" to mapOf("theme_mode" to true),
            "settings" to mapOf("badge_show_counts" to "yes"),
            "lockedApps" to listOf("not a key"),
        )
        damaged.forEach { field ->
            val tree = BackupFormat.encode(backup) + field
            assertEquals(field.toString(), BackupRead.Failed(BackupError.Malformed), decode(tree))
        }
        assertEquals(BackupRead.Failed(BackupError.Malformed), decode(BackupFormat.encode(backup) - "quietApps"))
    }

    @Test
    fun `a file from before app lock locks nothing`() {
        val read = decode(BackupFormat.encode(backup) - "lockedApps") as BackupRead.Read
        assertEquals(emptySet<AppKey>(), read.backup.lockedApps)
    }

    @Test
    fun `a file from before home's structure restores none of it`() {
        val structure = listOf("folders", "swipeLeft", "mutedOnHome", "dailyLimits", "routines")
        val tree = BackupFormat.encode(backup) - structure
        val read = decode(tree) as BackupRead.Read
        assertEquals(HomeStructure(), read.backup.structure)
        assertEquals(backup.favorites, read.backup.favorites)
    }

    @Test
    fun `a file from before now playing moved under the clock reads, its placement and switch ignored`() {
        val tree = BackupFormat.encode(backup) + mapOf(
            "nowPlayingAnchor" to "top",
            "liveRowsOff" to listOf("now_playing|${mail.encode()}", "notification_preview|${mail.encode()}"),
        )
        assertEquals(BackupRead.Read(backup), decode(tree))
    }

    @Test
    fun `damaged structure makes the whole file malformed`() {
        val damaged = listOf(
            "folders" to listOf(mapOf("id" to "talk", "name" to " ", "apps" to emptyList<String>())),
            "folders" to listOf(mapOf("id" to "talk", "name" to "Talk", "apps" to listOf("not a key"))),
            "swipeLeft" to mapOf(mail.encode() to "wave"),
            "mutedOnHome" to mail.encode(),
            "dailyLimits" to mapOf(mail.encode() to 0),
            "routines" to listOf(mapOf("start" to 1440, "end" to 60, "apps" to emptyList<String>())),
        )
        damaged.forEach { field ->
            val tree = BackupFormat.encode(backup) + field
            assertEquals(field.toString(), BackupRead.Failed(BackupError.Malformed), decode(tree))
        }
    }

    @Test
    fun `remapping moves home's structure with the other keys`() {
        val moved = backup.remapped(mainSerial = 5) { serial -> if (serial == 0L) 5L else null }
        val mailHere = mail.copy(userSerial = 5)
        assertEquals(listOf(HomeFolder("talk", "Talk", listOf(mailHere))), moved.structure.folders)
        assertEquals(mapOf(mailHere to SwipeAction.AppInfo), moved.structure.swipes)
        assertEquals(emptySet<AppKey>(), moved.structure.mutedApps)
        assertEquals(setOf(mailHere), moved.structure.routines.single().apps)
        assertEquals(setOf(0L, 10L), backup.serials)
    }

    @Test
    fun `a version 1 file written before headphone apps reads them as none`() {
        val read = decode(BackupFormat.encode(backup) - "headphoneApps") as BackupRead.Read
        assertEquals(emptySet<AppKey>(), read.backup.headphoneApps)
        assertEquals(
            BackupRead.Failed(BackupError.Malformed),
            decode(BackupFormat.encode(backup) + ("headphoneApps" to listOf("not a key"))),
        )
    }

    @Test
    fun `an unknown theme name reads as the default, as a downgrade would`() {
        val tree = BackupFormat.encode(backup) + ("settings" to mapOf("theme_mode" to "sepia"))
        val read = decode(tree) as BackupRead.Read
        assertEquals(ThemeMode.System, read.backup.settings.valueOf(ThemeSettings.Mode))
    }

    private fun decode(tree: Map<String, Any?>) = BackupFormat.decode(tree, backedUpSettings)

    private fun List<SettingValue<*>>.replace(value: SettingValue<*>) =
        map { if (it.setting == value.setting) value else it }

    private fun List<SettingValue<*>>.valueOf(setting: Any) = first { it.setting == setting }.value
}
