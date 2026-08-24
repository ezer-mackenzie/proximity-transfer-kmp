package io.github.ezer_mackenzie.proximitytransfer.core.transport.network

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.config.TransferLimits
import io.github.ezer_mackenzie.proximitytransfer.core.transport.Transport
import io.github.ezer_mackenzie.proximitytransfer.core.transport.TransportAlreadyOpenedException
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A [Transport] implementation backing [TransportCapability.LOCAL_NETWORK].
 */
class LocalNetworkTransport(
    private val maxPayloadSizeBytes: Long = TransferLimits.DEFAULT_MAX_PAYLOAD_BYTES,
    private val streamProvider: suspend () -> RawSocketStream,
) : Transport {
    val capability: TransportCapability = TransportCapability.LOCAL_NETWORK
    private val openMutex = Mutex()
    private var isOpened = false

    override suspend fun open(): Connection {
        openMutex.withLock {
            if (isOpened) {
                throw TransportAlreadyOpenedException("LocalNetworkTransport has already been opened")
            }
            isOpened = true
            val stream = streamProvider()
            return SocketConnection(
                stream = stream,
                maxPayloadSizeBytes = maxPayloadSizeBytes,
            )
        }
    }
}
