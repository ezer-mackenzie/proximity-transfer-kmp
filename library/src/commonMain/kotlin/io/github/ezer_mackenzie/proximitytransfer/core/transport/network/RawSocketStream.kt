package io.github.ezer_mackenzie.proximitytransfer.core.transport.network

import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.ConnectionClosedException

/**
 * A multiplatform continuous byte-stream boundary for network socket I/O.
 */
interface RawSocketStream {
    /**
     * Reads exactly [count] bytes into [buffer] starting at [offset].
     *
     * @throws ConnectionClosedException if the underlying stream closes before [count] bytes are read.
     */
    suspend fun readFully(buffer: ByteArray, offset: Int = 0, count: Int = buffer.size - offset)

    /**
     * Writes exactly [count] bytes from [buffer] starting at [offset].
     *
     * @throws ConnectionClosedException if the underlying stream is closed or disconnected.
     */
    suspend fun writeFully(buffer: ByteArray, offset: Int = 0, count: Int = buffer.size - offset)

    /** Closes the underlying socket stream resources. */
    suspend fun close()
}
