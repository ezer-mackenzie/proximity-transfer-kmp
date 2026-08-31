package io.github.ezer_mackenzie.proximitytransfer.core.session

/**
 * Serializable snapshot representing a saved checkpoint of an in-progress transfer session.
 */
data class SessionSnapshot(
    val sessionId: String,
    val payloadSha256Hex: String,
    val totalSize: Long,
    val chunkSize: Int,
    val bitmapBytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SessionSnapshot) return false
        if (sessionId != other.sessionId) return false
        if (payloadSha256Hex != other.payloadSha256Hex) return false
        if (totalSize != other.totalSize) return false
        if (chunkSize != other.chunkSize) return false
        if (!bitmapBytes.contentEquals(other.bitmapBytes)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = sessionId.hashCode()
        result = 31 * result + payloadSha256Hex.hashCode()
        result = 31 * result + totalSize.hashCode()
        result = 31 * result + chunkSize
        result = 31 * result + bitmapBytes.contentHashCode()
        return result
    }
}
