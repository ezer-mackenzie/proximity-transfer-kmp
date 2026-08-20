package dev.proximitytransfer.protocol

/** A wire-protocol version representable by one nonzero unsigned byte. */
class ProtocolVersion private constructor(val value: Int) {
    override fun equals(other: Any?): Boolean =
        this === other || other is ProtocolVersion && value == other.value

    override fun hashCode(): Int = value

    override fun toString(): String = "ProtocolVersion($value)"

    companion object {
        /** The only protocol version emitted and accepted by the current codec. */
        val Current = ProtocolVersion(1)

        /** Creates a version value without implying that the current codec supports it. */
        fun of(value: Int): ProtocolVersion {
            require(value in 1..UByte.MAX_VALUE.toInt()) {
                "Protocol version must be between 1 and 255"
            }
            return ProtocolVersion(value)
        }
    }
}
