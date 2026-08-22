package io.github.ezer_mackenzie.proximitytransfer.core.capability

/** Encodes transport capabilities in stable numeric-code order. */
object DeviceCapabilitiesCodec {
    private const val COUNT_SIZE = 1

    fun encode(capabilities: DeviceCapabilities): ByteArray {
        require(capabilities.transports.size <= UByte.MAX_VALUE.toInt()) {
            "Capability count cannot exceed 255"
        }

        val sorted = capabilities.transports.sortedBy { it.code }
        return ByteArray(COUNT_SIZE + sorted.size).also { encoded ->
            encoded[0] = sorted.size.toByte()
            sorted.forEachIndexed { index, capability ->
                encoded[COUNT_SIZE + index] = capability.code.toByte()
            }
        }
    }

    fun decode(encoded: ByteArray): DeviceCapabilities {
        if (encoded.isEmpty()) {
            throw DeviceCapabilitiesDecodingException("Capabilities payload is empty")
        }

        val count = encoded[0].toInt() and 0xFF
        if (count == 0 || encoded.size != COUNT_SIZE + count) {
            throw DeviceCapabilitiesDecodingException(
                "Declared capability count $count does not match the payload",
            )
        }

        val transports = buildSet {
            for (index in 0 until count) {
                val code = encoded[COUNT_SIZE + index].toInt() and 0xFF
                val capability = TransportCapability.fromCode(code)
                    ?: throw DeviceCapabilitiesDecodingException(
                        "Unknown transport capability code: $code",
                    )
                if (!add(capability)) {
                    throw DeviceCapabilitiesDecodingException(
                        "Duplicate transport capability: $capability",
                    )
                }
            }
        }
        return DeviceCapabilities(transports)
    }
}
