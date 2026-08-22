package io.github.ezer_mackenzie.proximitytransfer.core.capability

/** A transport feature that a peer can currently offer to a protocol session. */
enum class TransportCapability(
    val code: Int,
    val supportsBootstrap: Boolean,
    val supportsDataTransfer: Boolean,
) {
    MEMORY(1, supportsBootstrap = true, supportsDataTransfer = true),
    BLE(2, supportsBootstrap = true, supportsDataTransfer = true),
    LOCAL_NETWORK(3, supportsBootstrap = false, supportsDataTransfer = true),
    WIFI_AWARE(4, supportsBootstrap = false, supportsDataTransfer = true),
    WIFI_DIRECT(5, supportsBootstrap = false, supportsDataTransfer = true),
    MULTIPEER_CONNECTIVITY(6, supportsBootstrap = false, supportsDataTransfer = true),
    NFC_BOOTSTRAP(7, supportsBootstrap = true, supportsDataTransfer = false),
    QR_BOOTSTRAP(8, supportsBootstrap = true, supportsDataTransfer = false),
    ;

    companion object {
        fun fromCode(code: Int): TransportCapability? = entries.firstOrNull { it.code == code }
    }
}
