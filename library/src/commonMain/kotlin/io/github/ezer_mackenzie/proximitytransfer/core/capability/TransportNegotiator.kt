package io.github.ezer_mackenzie.proximitytransfer.core.capability

/** Selects deterministic compatible transports from two runtime capability sets. */
class TransportNegotiator(
    private val dataPreference: List<TransportCapability> = DEFAULT_DATA_PREFERENCE,
    private val bootstrapPreference: List<TransportCapability> = DEFAULT_BOOTSTRAP_PREFERENCE,
) {
    init {
        require(dataPreference.isNotEmpty()) { "Data preference cannot be empty" }
        require(dataPreference.distinct().size == dataPreference.size) {
            "Data preference cannot contain duplicates"
        }
        require(dataPreference.all { it.supportsDataTransfer }) {
            "Data preference contains a capability that cannot transfer data"
        }
        require(bootstrapPreference.distinct().size == bootstrapPreference.size) {
            "Bootstrap preference cannot contain duplicates"
        }
        require(bootstrapPreference.all { it.supportsBootstrap }) {
            "Bootstrap preference contains a capability that cannot bootstrap a session"
        }
    }

    fun negotiate(
        local: DeviceCapabilities,
        remote: DeviceCapabilities,
    ): NegotiationResult {
        val common = local.transports intersect remote.transports
        val dataTransport = dataPreference.firstOrNull { it in common }
            ?: throw NoCompatibleTransportException()
        val bootstrapTransport = bootstrapPreference.firstOrNull { it in common }

        return NegotiationResult(
            dataTransport = dataTransport,
            bootstrapTransport = bootstrapTransport,
        )
    }

    companion object {
        val DEFAULT_DATA_PREFERENCE: List<TransportCapability> = listOf(
            TransportCapability.LOCAL_NETWORK,
            TransportCapability.WIFI_AWARE,
            TransportCapability.WIFI_DIRECT,
            TransportCapability.MULTIPEER_CONNECTIVITY,
            TransportCapability.BLE,
            TransportCapability.MEMORY,
        )

        val DEFAULT_BOOTSTRAP_PREFERENCE: List<TransportCapability> = listOf(
            TransportCapability.NFC_BOOTSTRAP,
            TransportCapability.QR_BOOTSTRAP,
            TransportCapability.BLE,
            TransportCapability.MEMORY,
        )
    }
}
