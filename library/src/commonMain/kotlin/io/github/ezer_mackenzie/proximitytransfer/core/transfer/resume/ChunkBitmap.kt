package io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume

/**
 * Compact binary bitset tracking received vs missing chunk indices for a transfer session.
 *
 * @property totalChunks Total number of chunks declared in the transfer manifest.
 */
class ChunkBitmap(val totalChunks: Int, initialBytes: ByteArray? = null) {
    private val byteSize = (totalChunks + 7) / 8
    private val bits: ByteArray

    init {
        require(totalChunks > 0) { "Total chunks must be positive" }
        require(totalChunks <= MAX_SUPPORTED_CHUNKS) { "Total chunks $totalChunks exceeds maximum supported $MAX_SUPPORTED_CHUNKS" }
        if (initialBytes != null) {
            require(initialBytes.size == byteSize) {
                "Initial bitmap byte size ${initialBytes.size} does not match expected size $byteSize for $totalChunks chunks"
            }
            bits = initialBytes.copyOf()
        } else {
            bits = ByteArray(byteSize)
        }
    }

    /** Marks [chunkIndex] as received. */
    fun setReceived(chunkIndex: Int) {
        checkIndex(chunkIndex)
        val byteIndex = chunkIndex / 8
        val bitMask = 1 shl (chunkIndex % 8)
        bits[byteIndex] = (bits[byteIndex].toInt() or bitMask).toByte()
    }

    /** Returns `true` if [chunkIndex] has been received. */
    fun isReceived(chunkIndex: Int): Boolean {
        checkIndex(chunkIndex)
        val byteIndex = chunkIndex / 8
        val bitMask = 1 shl (chunkIndex % 8)
        return (bits[byteIndex].toInt() and bitMask) != 0
    }

    /** Returns a list of all chunk indices that have not been received yet. */
    fun missingChunkIndices(): List<Int> {
        val missing = ArrayList<Int>()
        for (i in 0 until totalChunks) {
            if (!isReceived(i)) {
                missing.add(i)
            }
        }
        return missing
    }

    /** Returns the underlying byte array representation of this bitmap. */
    fun encode(): ByteArray = bits.copyOf()

    private fun checkIndex(index: Int) {
        require(index in 0 until totalChunks) { "Chunk index $index out of bounds [0, $totalChunks)" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChunkBitmap) return false
        return totalChunks == other.totalChunks && bits.contentEquals(other.bits)
    }

    override fun hashCode(): Int {
        var result = totalChunks
        result = 31 * result + bits.contentHashCode()
        return result
    }

    companion object {
        const val MAX_SUPPORTED_CHUNKS = 1_000_000

        /** Decodes a [ChunkBitmap] from raw byte array [bytes] for [totalChunks]. */
        fun decode(totalChunks: Int, bytes: ByteArray): ChunkBitmap {
            return ChunkBitmap(totalChunks, bytes)
        }
    }
}
