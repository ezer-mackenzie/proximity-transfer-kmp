package io.github.ezer_mackenzie.proximitytransfer.core.integrity

import org.kotlincrypto.hash.sha2.SHA256

/** Computes SHA-256 digests using the multiplatform KotlinCrypto implementation. */
object Sha256 {
    const val DIGEST_SIZE: Int = 32

    fun digest(data: ByteArray): ByteArray = SHA256().digest(data)
}
