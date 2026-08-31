package io.github.ezer_mackenzie.proximitytransfer.core.security

import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection
import kotlin.random.Random

/**
 * Handles security handshake exchanges to derive dynamic [SessionKeySpec] symmetric keys between peers over a [Connection].
 */
class SessionKeyExchangeEngine(
    private val secureRandomBytes: (Int) -> ByteArray = { size -> Random.nextBytes(size) },
) {
    /**
     * Initiates and completes a key exchange handshake as the initiating peer.
     */
    suspend fun performInitiatorHandshake(connection: Connection, sessionToken: ByteArray): SessionKeySpec {
        val clientNonce = secureRandomBytes(16)
        val serverNonce = secureRandomBytes(16)
        val handshake = HandshakeMessage(clientNonce, serverNonce, sessionToken)

        connection.send(HandshakeMessageCodec.encode(handshake))
        val incomingBytes = connection.receive()
        val receivedHandshake = HandshakeMessageCodec.decode(incomingBytes)

        return receivedHandshake.deriveSessionKeySpec()
    }

    /**
     * Responds to and completes a key exchange handshake as the responding peer.
     */
    suspend fun performResponderHandshake(connection: Connection): SessionKeySpec {
        val incomingBytes = connection.receive()
        val receivedHandshake = HandshakeMessageCodec.decode(incomingBytes)

        connection.send(HandshakeMessageCodec.encode(receivedHandshake))
        return receivedHandshake.deriveSessionKeySpec()
    }
}
