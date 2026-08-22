package io.github.ezer_mackenzie.proximitytransfer.core.protocol

/** Identifies the semantic content carried by a protocol frame. */
enum class FrameType(val code: Int) {
    DATA(1),
    MANIFEST(2),
    CAPABILITIES(3),
    ;

    companion object {
        fun fromCode(code: Int): FrameType? = entries.firstOrNull { it.code == code }
    }
}
