package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.media.MediaState
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.core.notifications.Redaction
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class NotificationRowTest {
    private val phalanx = app("org.phalanx.android", "Phalanx")
    private val clock = app("com.android.deskclock", "Clock")
    private val workPhalanx = app("org.phalanx.android", "Phalanx", serial = 10)

    private fun app(pkg: String, label: String, serial: Long = 0) =
        LaunchableApp(AppKey(pkg, "$pkg.Main", serial), label, serial == 0L)

    private fun preview(owner: PackageUser, text: String) = NotificationPreview(
        key = "0|${owner.packageName}|$text",
        owner = owner,
        title = "Léa",
        text = text,
        redaction = Redaction.Full,
        contentIntent = null,
        autoCancel = true,
        isClearable = true,
        reply = null,
    )

    private fun owner(app: LaunchableApp) = PackageUser(app.key.packageName, app.key.userSerial)

    private fun shape(rows: List<HomeRow>) = rows.map {
        when (it) {
            is HomeRow.App -> it.app.label + if (it.app.isMainProfile) "" else " (work)"
            is NotificationRow -> "preview:" + it.preview.text
            is HomeRow.NowPlaying -> "now"
            HomeRow.ListenerPrompt -> "prompt"
            is CalendarRow -> "calendar"
            is WidgetHomeRow -> "widget"
        }
    }

    @Test
    fun `a preview sits right under its favourite, per profile`() {
        val rows = listOf(phalanx, clock, workPhalanx).map { HomeRow.App(it, AppBadge.None) }
        val previews = mapOf(owner(workPhalanx) to preview(owner(workPhalanx), "work"))
        assertEquals(listOf("Phalanx", "Clock", "Phalanx (work)", "preview:work"), shape(withPreviews(rows, previews)))
    }

    @Test
    fun `the preview comes before the favourite's now-playing row`() {
        val media = MediaState.Active(
            PlayingMedia(
                "org.phalanx.android",
                "Voice note",
                "Léa",
                art = null,
                artColor = null,
                isPlaying = true,
                canSkipNext = false,
                canSkipPrevious = false,
                canSeek = false,
                progress = null,
                upNext = emptyList(),
                output = null,
            ),
        )
        val base = assembleHomeRows(listOf(phalanx.key), listOf(phalanx), emptyMap(), media, showCounts = true)
        val rows = withPreviews(base, mapOf(owner(phalanx) to preview(owner(phalanx), "hi")))
        assertEquals(listOf("Phalanx", "preview:hi", "now"), shape(rows))
    }

    @Test
    fun `previews of apps that are not favourites are not shown`() {
        val rows = listOf(HomeRow.App(clock, AppBadge.None))
        assertSame(rows, withPreviews(rows, emptyMap()))
        assertEquals(listOf("Clock"), shape(withPreviews(rows, mapOf(owner(phalanx) to preview(owner(phalanx), "x")))))
    }

    @Test
    fun `the row is keyed by its app, so newer notifications update it in place`() {
        val first = NotificationRow(phalanx, preview(owner(phalanx), "one"))
        val second = NotificationRow(phalanx, preview(owner(phalanx), "two"))
        assertEquals(first.key, second.key)
    }
}
