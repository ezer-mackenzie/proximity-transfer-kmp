package io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume

/**
 * Request payload sent by a receiver to resume an interrupted transfer session.
 *
 * @property sessionToken Token identifying the session to be resumed.
 * @property lastReceivedChunkIndex Highest contiguous chunk index received, or -1 if no chunks received.
 * @property bitmap [ChunkBitmap] tracking individual received chunk states.
 */
data class ResumeRequest(
    val sessionToken: ByteArray,
    val lastReceivedChunkIndex: Int,
    val bitmap: ChunkBitmap,
) {
    init {
        require(sessionToken.isNotEmpty()) { "Session token cannot be empty" }
        require(lastReceivedChunkIndex >= -1) { "lastReceivedChunkIndex cannot be less than -1" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResumeRequest) return false
        return sessionToken.contentEquals(other.sessionToken) &&
            lastReceivedChunkIndex == other.lastReceivedChunkIndex &&
            bitmap == other.bitmap
    }

    override fun hashCode(): Int {
        var result = sessionToken.contentHashCode()
        result = 31 * result + lastReceivedChunkIndex
        result = 31 * result + bitmap.hashCode()
        return result
    }
}
