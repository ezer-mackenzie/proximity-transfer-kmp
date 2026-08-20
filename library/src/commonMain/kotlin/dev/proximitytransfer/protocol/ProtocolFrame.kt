package dev.proximitytransfer.protocol

/**
 * An immutable protocol frame.
 *
 * The supplied [payload] is copied during construction, and reading [payload]
 * returns another copy so callers cannot mutate frame contents.
 */
class ProtocolFrame(
    val version: ProtocolVersion,
    val type: FrameType,
    payload: ByteArray,
) {
    private val payloadBytes = payload.copyOf()

    val payloadSize: Int
        get() = payloadBytes.size

    val payload: ByteArray
        get() = payloadBytes.copyOf()

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is ProtocolFrame &&
            version == other.version &&
            type == other.type &&
            payloadBytes.contentEquals(other.payloadBytes)

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + payloadBytes.contentHashCode()
        return result
    }

    override fun toString(): String =
        "ProtocolFrame(version=$version, type=$type, payloadSize=$payloadSize)"
}
