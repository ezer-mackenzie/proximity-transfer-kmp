package io.github.ezer_mackenzie.proximitytransfer.core.security

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256

/**
 * Immutable specification holding a 32-byte secret key and a nonce prefix for frame payload encryption.
 *
 * @property secretKey 32-byte shared secret key.
 * @property noncePrefix 16-byte initial nonce prefix.
 */
class SessionKeySpec(
    val secretKey: ByteArray,
    val noncePrefix: ByteArray,
) {
    init {
        require(secretKey.size == KEY_SIZE) { "secretKey must be exactly $KEY_SIZE bytes" }
        require(noncePrefix.size == NONCE_PREFIX_SIZE) { "noncePrefix must be exactly $NONCE_PREFIX_SIZE bytes" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SessionKeySpec) return false
        return secretKey.contentEquals(other.secretKey) && noncePrefix.contentEquals(other.noncePrefix)
    }

    override fun hashCode(): Int {
        var result = secretKey.contentHashCode()
        result = 31 * result + noncePrefix.contentHashCode()
        return result
    }

    companion object {
        const val KEY_SIZE: Int = 32
        const val NONCE_PREFIX_SIZE: Int = 16
    }
}
