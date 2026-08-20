package io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256

/** Encodes the fixed-width binary transfer manifest. */
object TransferManifestCodec {
    private const val PAYLOAD_SIZE_OFFSET = 0
    private const val CHUNK_COUNT_OFFSET = 8
    private const val SHA256_OFFSET = 12
    const val ENCODED_SIZE: Int = SHA256_OFFSET + Sha256.DIGEST_SIZE

    fun encode(manifest: TransferManifest): ByteArray {
        val encoded = ByteArray(ENCODED_SIZE)
        writeLong(encoded, PAYLOAD_SIZE_OFFSET, manifest.payloadSize)
        writeInt(encoded, CHUNK_COUNT_OFFSET, manifest.chunkCount)
        manifest.sha256.copyInto(encoded, destinationOffset = SHA256_OFFSET)
        return encoded
    }

    fun decode(encoded: ByteArray): TransferManifest {
        if (encoded.size != ENCODED_SIZE) {
            throw TransferManifestDecodingException(
                "Manifest must contain exactly $ENCODED_SIZE bytes",
            )
        }

        val payloadSize = readLong(encoded, PAYLOAD_SIZE_OFFSET)
        val chunkCount = readInt(encoded, CHUNK_COUNT_OFFSET)
        if (payloadSize < 0 || chunkCount <= 0) {
            throw TransferManifestDecodingException("Manifest contains invalid size metadata")
        }

        return TransferManifest(
            payloadSize = payloadSize,
            chunkCount = chunkCount,
            sha256 = encoded.copyOfRange(SHA256_OFFSET, ENCODED_SIZE),
        )
    }

    private fun writeInt(destination: ByteArray, offset: Int, value: Int) {
        destination[offset] = (value ushr 24).toByte()
        destination[offset + 1] = (value ushr 16).toByte()
        destination[offset + 2] = (value ushr 8).toByte()
        destination[offset + 3] = value.toByte()
    }

    private fun readInt(source: ByteArray, offset: Int): Int =
        ((source[offset].toInt() and 0xFF) shl 24) or
            ((source[offset + 1].toInt() and 0xFF) shl 16) or
            ((source[offset + 2].toInt() and 0xFF) shl 8) or
            (source[offset + 3].toInt() and 0xFF)

    private fun writeLong(destination: ByteArray, offset: Int, value: Long) {
        for (byteIndex in 0 until Long.SIZE_BYTES) {
            val shift = (Long.SIZE_BYTES - 1 - byteIndex) * Byte.SIZE_BITS
            destination[offset + byteIndex] = (value ushr shift).toByte()
        }
    }

    private fun readLong(source: ByteArray, offset: Int): Long {
        var value = 0L
        for (byteIndex in 0 until Long.SIZE_BYTES) {
            value = (value shl Byte.SIZE_BITS) or
                (source[offset + byteIndex].toLong() and 0xFF)
        }
        return value
    }
}
