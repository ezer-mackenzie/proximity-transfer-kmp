package io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256

/** Immutable metadata required to receive and verify one payload transfer. */
class TransferManifest(
    val payloadSize: Long,
    val chunkCount: Int,
    sha256: ByteArray,
) {
    private val sha256Bytes = sha256.copyOf()

    init {
        require(payloadSize >= 0) { "Payload size cannot be negative" }
        require(chunkCount > 0) { "Chunk count must be positive" }
        require(sha256Bytes.size == Sha256.DIGEST_SIZE) {
            "SHA-256 digest must contain ${Sha256.DIGEST_SIZE} bytes"
        }
    }

    val sha256: ByteArray
        get() = sha256Bytes.copyOf()

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is TransferManifest &&
            payloadSize == other.payloadSize &&
            chunkCount == other.chunkCount &&
            sha256Bytes.contentEquals(other.sha256Bytes)

    override fun hashCode(): Int {
        var result = payloadSize.hashCode()
        result = 31 * result + chunkCount
        result = 31 * result + sha256Bytes.contentHashCode()
        return result
    }

    override fun toString(): String =
        "TransferManifest(payloadSize=$payloadSize, chunkCount=$chunkCount)"
}
