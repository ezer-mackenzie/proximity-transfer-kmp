package io.github.ezer_mackenzie.proximitytransfer.core.bootstrap

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability

/**
 * Metadata exchanged during bootstrapping (e.g. QR code or BLE) to connect data transports.
 */
data class BootstrapPayload(
    val peerId: String,
    val bootstrapCapability: TransportCapability,
    val targetCapability: TransportCapability,
    val host: String,
    val port: Int,
    val sessionToken: ByteArray,
) {
    init {
        require(peerId.isNotBlank()) { "peerId cannot be blank" }
        require(host.isNotBlank()) { "host cannot be blank" }
        require(port in 1..65535) { "port must be between 1 and 65535, but got $port" }
        require(sessionToken.isNotEmpty()) { "sessionToken cannot be empty" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BootstrapPayload) return false

        if (peerId != other.peerId) return false
        if (bootstrapCapability != other.bootstrapCapability) return false
        if (targetCapability != other.targetCapability) return false
        if (host != other.host) return false
        if (port != other.port) return false
        if (!sessionToken.contentEquals(other.sessionToken)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = peerId.hashCode()
        result = 31 * result + bootstrapCapability.hashCode()
        result = 31 * result + targetCapability.hashCode()
        result = 31 * result + host.hashCode()
        result = 31 * result + port
        result = 31 * result + sessionToken.contentHashCode()
        return result
    }
}
