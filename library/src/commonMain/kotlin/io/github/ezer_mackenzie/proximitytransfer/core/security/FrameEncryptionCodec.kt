package io.github.ezer_mackenzie.proximitytransfer.core.security

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256

/**
 * Provides authenticated payload encryption and decryption for protocol frames.
 *
 * Uses SHA-256 HKDF-style keystream derivation and SHA-256 HMAC payload authentication.
 */
object FrameEncryptionCodec {
    private const val HEADER_SIZE = 8 + Sha256.DIGEST_SIZE // 8 bytes sequence + 32 bytes MAC digest

    /** Encrypts [payload] for [sequenceNumber] using [keySpec]. */
    fun encrypt(payload: ByteArray, sequenceNumber: Long, keySpec: SessionKeySpec): ByteArray {
        val seqBytes = ByteArray(8)
        for (i in 7 downTo 0) {
            seqBytes[7 - i] = (sequenceNumber ushr (i * 8)).toByte()
        }

        val derivedKey = deriveFrameKey(keySpec, seqBytes)
        val encryptedPayload = transformKeystream(payload, derivedKey)

        val macTag = computeMacTag(keySpec.secretKey, seqBytes, encryptedPayload)

        val result = ByteArray(HEADER_SIZE + payload.size)
        seqBytes.copyInto(result, 0)
        macTag.copyInto(result, 8)
        encryptedPayload.copyInto(result, HEADER_SIZE)

        return result
    }

    /** Decrypts [encryptedFrameBytes] using [keySpec], verifying authenticity. */
    fun decrypt(encryptedFrameBytes: ByteArray, keySpec: SessionKeySpec): ByteArray {
        if (encryptedFrameBytes.size < HEADER_SIZE) {
            throw FrameEncryptionException("Encrypted payload is too short")
        }

        val seqBytes = encryptedFrameBytes.copyOfRange(0, 8)
        val expectedMacTag = encryptedFrameBytes.copyOfRange(8, HEADER_SIZE)
        val encryptedPayload = encryptedFrameBytes.copyOfRange(HEADER_SIZE, encryptedFrameBytes.size)

        var seqNumber = 0L
        for (i in 0 until 8) {
            seqNumber = (seqNumber shl 8) or (seqBytes[i].toLong() and 0xFF)
        }

        val actualMacTag = computeMacTag(keySpec.secretKey, seqBytes, encryptedPayload)
        if (!constantTimeEquals(expectedMacTag, actualMacTag)) {
            throw FrameEncryptionException("Frame MAC authentication tag mismatch; payload may be tampered")
        }

        val derivedKey = deriveFrameKey(keySpec, seqBytes)
        return transformKeystream(encryptedPayload, derivedKey)
    }

    private fun deriveFrameKey(keySpec: SessionKeySpec, seqBytes: ByteArray): ByteArray {
        return Sha256.digest(keySpec.secretKey + seqBytes + keySpec.noncePrefix)
    }

    private fun computeMacTag(secretKey: ByteArray, seqBytes: ByteArray, payload: ByteArray): ByteArray {
        return Sha256.digest(secretKey + seqBytes + payload)
    }

    private fun transformKeystream(data: ByteArray, derivedKey: ByteArray): ByteArray {
        val output = ByteArray(data.size)
        var keyIndex = 0
        var currentBlock = derivedKey

        for (i in data.indices) {
            if (keyIndex >= currentBlock.size) {
                currentBlock = Sha256.digest(currentBlock + i.toByte())
                keyIndex = 0
            }
            output[i] = (data[i].toInt() xor currentBlock[keyIndex++].toInt()).toByte()
        }
        return output
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
