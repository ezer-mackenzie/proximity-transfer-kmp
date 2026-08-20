package dev.proximitytransfer.protocol

/** Identifies the semantic content carried by a protocol frame. */
enum class FrameType(val code: Int) {
    DATA(1),
    ;

    companion object {
        fun fromCode(code: Int): FrameType? = entries.firstOrNull { it.code == code }
    }
}
