package io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk

/** Encodes chunk metadata followed by opaque chunk bytes. */
object PayloadChunkCodec {
    private const val HEADER_SIZE = 8

    fun encode(chunk: PayloadChunk): ByteArray {
        val data = chunk.data
        val encoded = ByteArray(HEADER_SIZE + data.size)
        writeInt(encoded, offset = 0, value = chunk.index)
        writeInt(encoded, offset = 4, value = chunk.total)
        data.copyInto(encoded, destinationOffset = HEADER_SIZE)
        return encoded
    }

    fun decode(encoded: ByteArray): PayloadChunk {
        if (encoded.size < HEADER_SIZE) {
            throw PayloadChunkDecodingException("Chunk is shorter than its $HEADER_SIZE-byte header")
        }

        val index = readInt(encoded, offset = 0)
        val total = readInt(encoded, offset = 4)
        if (total <= 0 || index !in 0 until total) {
            throw PayloadChunkDecodingException("Invalid chunk index $index for total $total")
        }

        return PayloadChunk(
            index = index,
            total = total,
            data = encoded.copyOfRange(HEADER_SIZE, encoded.size),
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
}
