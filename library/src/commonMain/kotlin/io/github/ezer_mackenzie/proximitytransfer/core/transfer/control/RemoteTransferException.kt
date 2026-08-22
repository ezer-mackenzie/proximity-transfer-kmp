package io.github.ezer_mackenzie.proximitytransfer.core.transfer.control

/** Indicates that the receiver explicitly rejected a transfer. */
class RemoteTransferException(
    val error: RemoteError,
) : IllegalStateException("Remote transfer failed with ${error.code}: ${error.message}")
