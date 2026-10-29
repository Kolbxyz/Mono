package com.wander.mono.data.backup

import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.LiveRowKind
import com.wander.mono.data.LiveRowRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileRemapTest {
    @Test
    fun `the backup's own profile becomes this phone's`() {
        val remap = profileRemap(backupMain = 0, backupSerials = setOf(0), main = 7, others = emptySet())
        assertEquals(7L, remap(0))
    }

    @Test
    fun `a profile this phone also has keeps its serial`() {
        val remap = profileRemap(backupMain = 0, backupSerials = setOf(0, 10, 11), main = 0, others = setOf(10, 11))
        assertEquals(10L, remap(10))
        assertEquals(11L, remap(11))
    }

    @Test
    fun `one other profile on each side maps to the other`() {
        val remap = profileRemap(backupMain = 0, backupSerials = setOf(0, 10), main = 0, others = setOf(12))
        assertEquals(12L, remap(10))
    }

    @Test
    fun `other profiles with no clear match are dropped`() {
        assertNull(profileRemap(0, setOf(0, 10), main = 0, others = emptySet())(10))
        assertNull(profileRemap(0, setOf(0, 10, 11), main = 0, others = setOf(12))(10))
        assertNull(profileRemap(0, setOf(0, 10), main = 0, others = setOf(12, 13))(10))
    }

    @Test
    fun `remapping moves every key and drops those without a profile`() {
        val mail = AppKey("org.mail", "org.mail.Main", 0)
        val work = AppKey("org.chat", "org.chat.Main", 10)
        val backup = Backup(
            mainSerial = 0,
            favorites = listOf(work, mail),
            hidden = setOf(work),
            labels = mapOf(mail to "Post", work to "Chat"),
            liveRowsOff = setOf(LiveRowRef(mail, LiveRowKind.Calendar), LiveRowRef(work, LiveRowKind.Calendar)),
            quietApps = setOf(mail, work),
            settings = emptyList(),
        )
        val moved = backup.remapped(mainSerial = 5) { serial -> if (serial == 0L) 5L else null }
        val mailHere = mail.copy(userSerial = 5)
        assertEquals(
            Backup(
                mainSerial = 5,
                favorites = listOf(mailHere),
                hidden = emptySet(),
                labels = mapOf(mailHere to "Post"),
                liveRowsOff = setOf(LiveRowRef(mailHere, LiveRowKind.Calendar)),
                quietApps = setOf(mailHere),
                settings = emptyList(),
            ),
            moved,
        )
        assertEquals(setOf(0L, 10L), backup.serials)
    }
}
