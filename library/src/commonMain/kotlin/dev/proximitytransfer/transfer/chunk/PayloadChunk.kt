package dev.proximitytransfer.transfer.chunk

/** One immutable, indexed portion of a payload. */
class PayloadChunk(
    val index: Int,
    val total: Int,
    data: ByteArray,
) {
    private val dataBytes = data.copyOf()

    init {
        require(total > 0) { "Chunk total must be positive" }
        require(index in 0 until total) { "Chunk index $index is outside 0 until $total" }
    }

    val size: Int
        get() = dataBytes.size

    val data: ByteArray
        get() = dataBytes.copyOf()

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is PayloadChunk &&
            index == other.index &&
            total == other.total &&
            dataBytes.contentEquals(other.dataBytes)

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + total
        result = 31 * result + dataBytes.contentHashCode()
        return result
    }

    override fun toString(): String =
        "PayloadChunk(index=$index, total=$total, size=$size)"
}
