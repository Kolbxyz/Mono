package com.wander.mono.core.security

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** What opening a sealed file gave. */
sealed interface Unsealed {
    class Plain(val bytes: ByteArray) : Unsealed

    /** The tag did not match: another passphrase, or a changed byte anywhere in the file. */
    data object WrongPassphrase : Unsealed

    /** Sealed by a newer Mono, in a layout this build cannot read. */
    data object Newer : Unsealed

    /** Too short, or a header no Mono writes. */
    data object Damaged : Unsealed
}

/**
 * A backup sealed with a passphrase: AES-256-GCM under a key stretched from the passphrase with
 * PBKDF2-HMAC-SHA256, a fresh random salt and IV each time. The file is a header, then the
 * ciphertext with its tag:
 *
 * `MONOSEAL` · version (1 byte) · iterations (4 bytes) · salt (16) · IV (12) · ciphertext + tag (16)
 *
 * The header is the cipher's associated data, so a changed byte in it fails like a wrong
 * passphrase. Plain `javax.crypto`: it runs the same in local unit tests.
 */
object BackupCipher {
    const val ITERATIONS = 310_000

    private val MAGIC = "MONOSEAL".toByteArray(Charsets.US_ASCII)
    private const val VERSION: Byte = 1
    private const val KDF = "PBKDF2WithHmacSHA256"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    /** Far beyond any Mono's own count; caps the work a crafted file can ask for. */
    private const val MAX_ITERATIONS = 10_000_000
    private val HEADER_BYTES = MAGIC.size + 1 + Int.SIZE_BYTES + SALT_BYTES + IV_BYTES

    /** Whether [bytes] start like a sealed backup, so the passphrase is asked before anything else. */
    fun isSealed(bytes: ByteArray): Boolean =
        bytes.size >= MAGIC.size && MAGIC.indices.all { bytes[it] == MAGIC[it] }

    fun seal(plain: ByteArray, passphrase: CharArray, random: SecureRandom = SecureRandom()): ByteArray {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)
        val header = ByteBuffer.allocate(HEADER_BYTES)
            .put(MAGIC)
            .put(VERSION)
            .putInt(ITERATIONS)
            .put(salt)
            .put(iv)
            .array()
        val cipher = cipher(Cipher.ENCRYPT_MODE, key(passphrase, salt, ITERATIONS), iv)
        cipher.updateAAD(header)
        return header + cipher.doFinal(plain)
    }

    fun open(file: ByteArray, passphrase: CharArray): Unsealed {
        if (!isSealed(file) || file.size < MAGIC.size + 1) return Unsealed.Damaged
        val version = file[MAGIC.size]
        if (version > VERSION) return Unsealed.Newer
        if (version < VERSION || file.size < HEADER_BYTES + TAG_BITS / Byte.SIZE_BITS) return Unsealed.Damaged
        val header = ByteBuffer.wrap(file, MAGIC.size + 1, HEADER_BYTES - MAGIC.size - 1)
        val iterations = header.int
        if (iterations !in ITERATIONS..MAX_ITERATIONS) return Unsealed.Damaged
        val salt = ByteArray(SALT_BYTES).also { header.get(it) }
        val iv = ByteArray(IV_BYTES).also { header.get(it) }
        val cipher = cipher(Cipher.DECRYPT_MODE, key(passphrase, salt, iterations), iv)
        cipher.updateAAD(file, 0, HEADER_BYTES)
        return try {
            Unsealed.Plain(cipher.doFinal(file, HEADER_BYTES, file.size - HEADER_BYTES))
        } catch (_: AEADBadTagException) {
            Unsealed.WrongPassphrase
        }
    }

    private fun cipher(mode: Int, key: SecretKeySpec, iv: ByteArray): Cipher =
        Cipher.getInstance(TRANSFORMATION).apply { init(mode, key, GCMParameterSpec(TAG_BITS, iv)) }

    /** The stretched key; the spec's copy of the passphrase and the raw key bytes are wiped after. */
    private fun key(passphrase: CharArray, salt: ByteArray, iterations: Int): SecretKeySpec {
        val spec = PBEKeySpec(passphrase, salt, iterations, KEY_BITS)
        try {
            val raw = SecretKeyFactory.getInstance(KDF).generateSecret(spec).encoded
            return SecretKeySpec(raw, "AES").also { raw.fill(0) }
        } finally {
            spec.clearPassword()
        }
    }
}
