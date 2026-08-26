package io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256

/**
 * Represents a manifest containing metadata for multiple payload items in a single transfer session.
 *
 * @property items List of [ManifestItem] entries included in this transfer.
 * @property overallSha256 32-byte SHA-256 digest of the overall transfer manifest/stream.
 */
data class MultiItemManifest(
    val items: List<ManifestItem>,
    val overallSha256: ByteArray,
) {
    /** Total cumulative size of all payload items in bytes. */
    val totalSize: Long = items.sumOf { it.size }

    init {
        require(items.isNotEmpty()) { "MultiItemManifest must contain at least one item" }
        require(overallSha256.size == Sha256.DIGEST_SIZE) { "Overall SHA-256 must be exactly ${Sha256.DIGEST_SIZE} bytes" }
        val ids = items.map { it.id }
        require(ids.distinct().size == ids.size) { "Item IDs in MultiItemManifest must be unique" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultiItemManifest) return false
        return items == other.items && overallSha256.contentEquals(other.overallSha256)
    }

    override fun hashCode(): Int {
        var result = items.hashCode()
        result = 31 * result + overallSha256.contentHashCode()
        return result
    }
}
