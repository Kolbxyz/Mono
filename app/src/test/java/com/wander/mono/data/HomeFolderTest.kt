package com.wander.mono.data

import com.wander.mono.core.apps.AppKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeFolderTest {
    private val mail = AppKey("org.mail", "org.mail.Main", 0)
    private val chat = AppKey("org.chat", "org.chat.Main", 10)
    private val maps = AppKey("org.maps", "org.maps.Main", 0)

    @Test
    fun `a folder survives encoding and decoding`() {
        val folder = HomeFolder("a1", "Talk", listOf(chat, mail))
        assertEquals(folder, HomeFolder.decode(folder.encode()))
    }

    @Test
    fun `names are kept on one line`() {
        assertEquals("Work stuff", cleanFolderName("  Work\t\nstuff "))
        val folder = HomeFolder("a1", "Work\tstuff", listOf(mail))
        assertEquals("Work stuff", HomeFolder.decode(folder.encode())?.name)
    }

    @Test
    fun `damaged lines are not folders`() {
        assertNull(HomeFolder.decode(""))
        assertNull(HomeFolder.decode("a1"))
        assertNull(HomeFolder.decode("a1\t "))
        assertNull(HomeFolder.decode("a1\tTalk\tnot a key"))
    }

    @Test
    fun `an app moves into a folder and out of any other, and emptied folders go`() {
        val folders = listOf(HomeFolder("a", "A", listOf(mail)), HomeFolder("b", "B", listOf(chat)))
        assertEquals(listOf(HomeFolder("b", "B", listOf(chat, mail))), folders.withApp("b", mail))
    }

    @Test
    fun `adding an app already there puts it last`() {
        val folders = listOf(HomeFolder("a", "A", listOf(mail, chat)))
        assertEquals(listOf(HomeFolder("a", "A", listOf(chat, mail))), folders.withApp("a", mail))
    }

    @Test
    fun `taking an app out leaves the rest`() {
        val folders = listOf(HomeFolder("a", "A", listOf(mail, maps)), HomeFolder("b", "B", listOf(chat)))
        assertEquals(listOf(HomeFolder("a", "A", listOf(maps))), folders.withoutApp(mail).withoutApp(chat))
    }
}
