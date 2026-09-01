package io.github.ezer_mackenzie.proximitytransfer.core.security

import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.ControlMessageDecodingException
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection
import kotlin.random.Random

/**
 * Handles security handshake exchanges to derive dynamic [SessionKeySpec] symmetric keys between peers over a [Connection].
 * Both initiator and responder contribute independent 16-byte random nonces to derive the shared secret key.
 */
class SessionKeyExchangeEngine(
    private val secureRandomBytes: (Int) -> ByteArray = { size -> Random.nextBytes(size) },
) {
    /**
     * Initiates and completes a key exchange handshake as the initiating peer.
     *
     * 1. Generates [clientNonce] and sends an initial handshake containing [sessionToken].
     * 2. Receives responder's handshake containing [serverNonce].
     * 3. Verifies [clientNonce] and derives [SessionKeySpec].
     */
    suspend fun performInitiatorHandshake(connection: Connection, sessionToken: ByteArray): SessionKeySpec {
        val clientNonce = secureRandomBytes(HandshakeMessage.NONCE_SIZE)
        val initialHandshake = HandshakeMessage(
            clientNonce = clientNonce,
            serverNonce = ByteArray(HandshakeMessage.NONCE_SIZE),
            sessionToken = sessionToken,
        )

        connection.send(HandshakeMessageCodec.encode(initialHandshake))

        val incomingBytes = connection.receive()
        val responseHandshake = HandshakeMessageCodec.decode(incomingBytes)

        if (!clientNonce.contentEquals(responseHandshake.clientNonce)) {
            throw ControlMessageDecodingException("Handshake verification failed: clientNonce mismatch")
        }
        if (!sessionToken.contentEquals(responseHandshake.sessionToken)) {
            throw ControlMessageDecodingException("Handshake verification failed: sessionToken mismatch")
        }

        return responseHandshake.deriveSessionKeySpec()
    }

    /**
     * Responds to and completes a key exchange handshake as the responding peer.
     *
     * 1. Receives initial handshake and extracts initiator's [clientNonce] and [sessionToken].
     * 2. Generates an independent [serverNonce].
     * 3. Transmits completed handshake back to initiator and derives [SessionKeySpec].
     */
    suspend fun performResponderHandshake(connection: Connection): SessionKeySpec {
        val incomingBytes = connection.receive()
        val initialHandshake = HandshakeMessageCodec.decode(incomingBytes)

        val serverNonce = secureRandomBytes(HandshakeMessage.NONCE_SIZE)
        val responseHandshake = HandshakeMessage(
            clientNonce = initialHandshake.clientNonce,
            serverNonce = serverNonce,
            sessionToken = initialHandshake.sessionToken,
        )

        connection.send(HandshakeMessageCodec.encode(responseHandshake))
        return responseHandshake.deriveSessionKeySpec()
    }
}
