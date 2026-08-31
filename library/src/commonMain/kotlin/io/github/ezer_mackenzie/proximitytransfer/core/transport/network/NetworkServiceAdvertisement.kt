package io.github.ezer_mackenzie.proximitytransfer.core.transport.network

/**
 * Domain model representing a local network service advertisement for peer discovery.
 */
data class NetworkServiceAdvertisement(
    val serviceName: String,
    val host: String,
    val port: Int,
    val sessionToken: String,
)
