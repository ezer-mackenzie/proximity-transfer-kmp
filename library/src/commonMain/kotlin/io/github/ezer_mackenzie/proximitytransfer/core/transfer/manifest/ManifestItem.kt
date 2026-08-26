package io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256

/**
 * Represents a single file or payload item within a [MultiItemManifest].
 *
 * @property id Unique identifier for the item within the manifest.
 * @property relativePath Relative destination path or file name.
 * @property size Size of the item payload in bytes.
 * @property sha256 Expected 32-byte SHA-256 digest of the item.
 */
data class ManifestItem(
    val id: String,
    val relativePath: String,
    val size: Long,
    val sha256: ByteArray,
) {
    init {
        require(id.isNotBlank()) { "Item ID cannot be blank" }
        require(relativePath.isNotBlank()) { "Relative path cannot be blank" }
        require(size >= 0) { "Item size cannot be negative" }
        require(sha256.size == Sha256.DIGEST_SIZE) { "SHA-256 must be exactly ${Sha256.DIGEST_SIZE} bytes" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ManifestItem) return false
        return id == other.id &&
            relativePath == other.relativePath &&
            size == other.size &&
            sha256.contentEquals(other.sha256)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + relativePath.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + sha256.contentHashCode()
        return result
    }
}
