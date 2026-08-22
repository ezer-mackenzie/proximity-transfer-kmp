package io.github.ezer_mackenzie.proximitytransfer.core.transfer.control

/** Stable error categories that can be reported to a remote peer. */
enum class RemoteErrorCode(val code: Int) {
    MALFORMED_TRANSFER(1),
    INTEGRITY_FAILURE(2),
    ;

    companion object {
        fun fromCode(code: Int): RemoteErrorCode? = entries.firstOrNull { it.code == code }
    }
}
