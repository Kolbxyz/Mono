package com.wander.mono.core.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCipherTest {
    private val plain = """{"format":"mono-backup","version":1}""".toByteArray()
    private val passphrase = "correct horse battery".toCharArray()

    @Test
    fun `a sealed backup opens with its passphrase`() {
        val sealed = BackupCipher.seal(plain, passphrase.copyOf())
        val opened = BackupCipher.open(sealed, passphrase.copyOf())
        assertArrayEquals(plain, (opened as Unsealed.Plain).bytes)
    }

    @Test
    fun `another passphrase does not open it`() {
        val sealed = BackupCipher.seal(plain, passphrase.copyOf())
        assertEquals(Unsealed.WrongPassphrase, BackupCipher.open(sealed, "wrong horse".toCharArray()))
    }

    @Test
    fun `a sealed file is recognised and a plain one is not`() {
        assertTrue(BackupCipher.isSealed(BackupCipher.seal(plain, passphrase.copyOf())))
        assertFalse(BackupCipher.isSealed(plain))
        assertFalse(BackupCipher.isSealed(ByteArray(0)))
    }

    @Test
    fun `the plain text never shows and each sealing is different`() {
        val first = BackupCipher.seal(plain, passphrase.copyOf())
        val second = BackupCipher.seal(plain, passphrase.copyOf())
        assertFalse(first.decodeToString(throwOnInvalidSequence = false).contains("mono-backup"))
        assertNotEquals(first.toList(), second.toList())
    }

    @Test
    fun `a changed byte in the header or the body fails like a wrong passphrase`() {
        val sealed = BackupCipher.seal(plain, passphrase.copyOf())
        listOf(HEADER_SALT_OFFSET, sealed.lastIndex).forEach { index ->
            val damaged = sealed.copyOf().also { it[index] = (it[index] + 1).toByte() }
            assertEquals(Unsealed.WrongPassphrase, BackupCipher.open(damaged, passphrase.copyOf()))
        }
    }

    @Test
    fun `a newer version is refused and a cut file is damaged`() {
        val sealed = BackupCipher.seal(plain, passphrase.copyOf())
        val newer = sealed.copyOf().also { it[VERSION_OFFSET] = 2 }
        assertEquals(Unsealed.Newer, BackupCipher.open(newer, passphrase.copyOf()))
        assertEquals(Unsealed.Damaged, BackupCipher.open(sealed.copyOf(HEADER_SALT_OFFSET), passphrase.copyOf()))
    }

    @Test
    fun `an iteration count below Mono's own is refused`() {
        val sealed = BackupCipher.seal(plain, passphrase.copyOf())
        // The four iteration bytes follow the version; zero them.
        val weakened = sealed.copyOf().also { bytes -> (0 until 4).forEach { bytes[VERSION_OFFSET + 1 + it] = 0 } }
        assertEquals(Unsealed.Damaged, BackupCipher.open(weakened, passphrase.copyOf()))
    }

    private companion object {
        /** "MONOSEAL" is 8 bytes, then the version byte, then 4 bytes of iterations, then the salt. */
        const val VERSION_OFFSET = 8
        const val HEADER_SALT_OFFSET = 13
    }
}
