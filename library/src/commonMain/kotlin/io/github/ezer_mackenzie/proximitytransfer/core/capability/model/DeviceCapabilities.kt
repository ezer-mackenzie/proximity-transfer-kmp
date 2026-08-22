package io.github.ezer_mackenzie.proximitytransfer.core.capability.model

/** Immutable set of transport capabilities currently available on one peer. */
class DeviceCapabilities(
    transports: Set<TransportCapability>,
) {
    private val transportSet = transports.toSet()

    init {
        require(transportSet.isNotEmpty()) { "At least one transport capability is required" }
    }

    val transports: Set<TransportCapability>
        get() = transportSet.toSet()

    override fun equals(other: Any?): Boolean =
        this === other || other is DeviceCapabilities && transportSet == other.transportSet

    override fun hashCode(): Int = transportSet.hashCode()

    override fun toString(): String = "DeviceCapabilities(transports=$transportSet)"
}
