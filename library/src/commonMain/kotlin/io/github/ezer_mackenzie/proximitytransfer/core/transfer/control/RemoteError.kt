package io.github.ezer_mackenzie.proximitytransfer.core.transfer.control

/** A bounded protocol error safe to report to the remote peer. */
class RemoteError(
    val code: RemoteErrorCode,
    val message: String,
) {
    init {
        require(message.encodeToByteArray().size <= RemoteErrorCodec.MAX_MESSAGE_SIZE) {
            "Remote error message is too large"
        }
    }
}
