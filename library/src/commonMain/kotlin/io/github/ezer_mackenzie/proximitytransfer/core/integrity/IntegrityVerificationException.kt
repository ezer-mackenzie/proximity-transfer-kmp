package io.github.ezer_mackenzie.proximitytransfer.core.integrity

/** Indicates that reconstructed content does not match its declared integrity metadata. */
class IntegrityVerificationException(message: String) : IllegalArgumentException(message)
