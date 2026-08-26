package io.github.ezer_mackenzie.proximitytransfer.core.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FrameEncryptionCodecTest {

    private val keySpec = SessionKeySpec(
        secretKey = ByteArray(32) { (it + 1).toByte() },
        noncePrefix = ByteArray(16) { (it + 10).toByte() },
    )

    @Test
    fun encryptAndDecryptRoundTrip() {
        val originalPayload = "Hello Proximity Transfer Security!".encodeToByteArray()
        val sequenceNumber = 42L

        val encrypted = FrameEncryptionCodec.encrypt(originalPayload, sequenceNumber, keySpec)
        val decrypted = FrameEncryptionCodec.decrypt(encrypted, keySpec)

        assertEquals(originalPayload.toList(), decrypted.toList())
    }

    @Test
    fun failsOnTamperedMacTag() {
        val originalPayload = "Secret Payload Bytes".encodeToByteArray()
        val encrypted = FrameEncryptionCodec.encrypt(originalPayload, 1L, keySpec)

        // Corrupt MAC tag byte
        encrypted[10] = (encrypted[10].toInt() xor 0xFF).toByte()

        assertFailsWith<FrameEncryptionException> {
            FrameEncryptionCodec.decrypt(encrypted, keySpec)
        }
    }

    @Test
    fun failsOnTamperedEncryptedPayload() {
        val originalPayload = "Secret Payload Bytes".encodeToByteArray()
        val encrypted = FrameEncryptionCodec.encrypt(originalPayload, 1L, keySpec)

        // Corrupt payload byte
        encrypted[encrypted.size - 1] = (encrypted[encrypted.size - 1].toInt() xor 0xFF).toByte()

        assertFailsWith<FrameEncryptionException> {
            FrameEncryptionCodec.decrypt(encrypted, keySpec)
        }
    }

    @Test
    fun failsOnTooShortEncryptedBytes() {
        assertFailsWith<FrameEncryptionException> {
            FrameEncryptionCodec.decrypt(byteArrayOf(1, 2, 3, 4), keySpec)
        }
    }
}
