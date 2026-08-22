package io.github.ezer_mackenzie.proximitytransfer.core.capability

/** The compatible bootstrap and data paths selected for two peers. */
class NegotiationResult(
    val dataTransport: TransportCapability,
    val bootstrapTransport: TransportCapability?,
) {
    init {
        require(dataTransport.supportsDataTransfer) {
            "$dataTransport cannot transfer payload data"
        }
        require(bootstrapTransport == null || bootstrapTransport.supportsBootstrap) {
            "$bootstrapTransport cannot bootstrap a session"
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is NegotiationResult &&
            dataTransport == other.dataTransport &&
            bootstrapTransport == other.bootstrapTransport

    override fun hashCode(): Int = 31 * dataTransport.hashCode() + bootstrapTransport.hashCode()

    override fun toString(): String =
        "NegotiationResult(dataTransport=$dataTransport, bootstrapTransport=$bootstrapTransport)"
}
