package dev.proximitytransfer.protocol

class ProtocolVersion private constructor(val value: Int) {
    override fun equals(other: Any?): Boolean =
        this === other || other is ProtocolVersion && value == other.value

    override fun hashCode(): Int = value

    override fun toString(): String = "ProtocolVersion($value)"

    companion object {
        val Current = ProtocolVersion(1)

        fun of(value: Int): ProtocolVersion {
            require(value in 1..UByte.MAX_VALUE.toInt()) {
                "Protocol version must be between 1 and 255"
            }
            return ProtocolVersion(value)
        }
    }
}
