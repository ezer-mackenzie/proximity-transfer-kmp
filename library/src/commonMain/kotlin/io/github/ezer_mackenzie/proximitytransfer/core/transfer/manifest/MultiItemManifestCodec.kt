package io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256

/** Binary encoder/decoder for [MultiItemManifest]. */
object MultiItemManifestCodec {
    private const val VERSION: Byte = 0x01

    /** Encodes [manifest] into a versioned binary ByteArray representation. */
    fun encode(manifest: MultiItemManifest): ByteArray {
        var totalBytes = 1 + 4 // Version (1) + Item Count (4)
        val itemsData = manifest.items.map { item ->
            val idBytes = item.id.encodeToByteArray()
            val pathBytes = item.relativePath.encodeToByteArray()
            require(idBytes.size <= 0xFFFF) { "Item ID exceeds maximum length" }
            require(pathBytes.size <= 0xFFFF) { "Item relativePath exceeds maximum length" }
            totalBytes += 2 + idBytes.size + 2 + pathBytes.size + 8 + Sha256.DIGEST_SIZE
            Triple(idBytes, pathBytes, item)
        }
        totalBytes += Sha256.DIGEST_SIZE // Overall SHA-256

        val buffer = ByteArray(totalBytes)
        var offset = 0

        buffer[offset++] = VERSION

        val count = manifest.items.size
        buffer[offset++] = (count shr 24).toByte()
        buffer[offset++] = (count shr 16).toByte()
        buffer[offset++] = (count shr 8).toByte()
        buffer[offset++] = count.toByte()

        itemsData.forEach { (idBytes, pathBytes, item) ->
            buffer[offset++] = (idBytes.size shr 8).toByte()
            buffer[offset++] = idBytes.size.toByte()
            idBytes.copyInto(buffer, offset)
            offset += idBytes.size

            buffer[offset++] = (pathBytes.size shr 8).toByte()
            buffer[offset++] = pathBytes.size.toByte()
            pathBytes.copyInto(buffer, offset)
            offset += pathBytes.size

            val size = item.size
            for (i in 7 downTo 0) {
                buffer[offset++] = (size ushr (i * 8)).toByte()
            }

            item.sha256.copyInto(buffer, offset)
            offset += Sha256.DIGEST_SIZE
        }

        manifest.overallSha256.copyInto(buffer, offset)
        return buffer
    }

    /** Decodes [bytes] into a [MultiItemManifest]. */
    fun decode(bytes: ByteArray): MultiItemManifest {
        if (bytes.size < 1 + 4 + Sha256.DIGEST_SIZE) {
            throw TransferManifestDecodingException("Manifest payload too short for multi-item header")
        }

        var offset = 0
        val version = bytes[offset++]
        if (version != VERSION) {
            throw TransferManifestDecodingException("Unsupported multi-item manifest version $version")
        }

        val itemCount = ((bytes[offset++].toInt() and 0xFF) shl 24) or
            ((bytes[offset++].toInt() and 0xFF) shl 16) or
            ((bytes[offset++].toInt() and 0xFF) shl 8) or
            (bytes[offset++].toInt() and 0xFF)

        if (itemCount <= 0) {
            throw TransferManifestDecodingException("Invalid item count $itemCount in multi-item manifest")
        }

        val items = ArrayList<ManifestItem>(itemCount)

        fun readLengthPrefixedString(): String {
            if (offset + 2 > bytes.size) throw TransferManifestDecodingException("Unexpected EOF reading string length")
            val len = ((bytes[offset++].toInt() and 0xFF) shl 8) or (bytes[offset++].toInt() and 0xFF)
            if (offset + len > bytes.size) throw TransferManifestDecodingException("Unexpected EOF reading string bytes")
            val str = bytes.copyOfRange(offset, offset + len).decodeToString()
            offset += len
            return str
        }

        for (i in 0 until itemCount) {
            val id = readLengthPrefixedString()
            val relativePath = readLengthPrefixedString()

            if (offset + 8 + Sha256.DIGEST_SIZE > bytes.size) {
                throw TransferManifestDecodingException("Unexpected EOF reading item numeric fields")
            }

            var size = 0L
            for (j in 0 until 8) {
                size = (size shl 8) or (bytes[offset++].toLong() and 0xFF)
            }

            val itemSha256 = bytes.copyOfRange(offset, offset + Sha256.DIGEST_SIZE)
            offset += Sha256.DIGEST_SIZE

            try {
                items.add(ManifestItem(id, relativePath, size, itemSha256))
            } catch (e: IllegalArgumentException) {
                throw TransferManifestDecodingException("Invalid manifest item attributes: ${e.message}", e)
            }
        }

        if (offset + Sha256.DIGEST_SIZE > bytes.size) {
            throw TransferManifestDecodingException("Unexpected EOF reading overall SHA-256 digest")
        }

        val overallSha256 = bytes.copyOfRange(offset, offset + Sha256.DIGEST_SIZE)
        try {
            return MultiItemManifest(items, overallSha256)
        } catch (e: IllegalArgumentException) {
            throw TransferManifestDecodingException("Invalid multi-item manifest: ${e.message}", e)
        }
    }
}
