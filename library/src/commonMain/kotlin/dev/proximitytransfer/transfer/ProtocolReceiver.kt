package dev.proximitytransfer.transfer

import dev.proximitytransfer.protocol.FrameType
import dev.proximitytransfer.protocol.ProtocolFrameCodec
import dev.proximitytransfer.transport.connection.Connection

/** Receives and validates versioned protocol frames from a [Connection]. */
class ProtocolReceiver(
    private val connection: Connection,
) {
    /** Waits for one `DATA` frame and returns a copy of its payload. */
    suspend fun receive(): ByteArray {
        val frame = ProtocolFrameCodec.decode(connection.receive())
        require(frame.type == FrameType.DATA) {
            "Expected a DATA frame but received ${frame.type}"
        }
        return frame.payload
    }
}
