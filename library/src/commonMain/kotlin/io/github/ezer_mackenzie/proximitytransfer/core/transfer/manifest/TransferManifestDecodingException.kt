package io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest

/** Indicates that received bytes do not contain a valid transfer manifest. */
class TransferManifestDecodingException(message: String) : IllegalArgumentException(message)
