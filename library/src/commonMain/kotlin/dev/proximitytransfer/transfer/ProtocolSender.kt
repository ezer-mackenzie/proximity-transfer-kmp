package dev.proximitytransfer.transfer

import dev.proximitytransfer.protocol.FrameType
import dev.proximitytransfer.protocol.ProtocolFrame
import dev.proximitytransfer.protocol.ProtocolFrameCodec
import dev.proximitytransfer.protocol.ProtocolVersion
import dev.proximitytransfer.transport.connection.Connection

/** Sends application payloads as versioned protocol frames over a [Connection]. */
class ProtocolSender(
    private val connection: Connection,
) {
    /** Sends one payload without taking ownership of the caller's mutable array. */
    suspend fun send(payload: ByteArray) {
        val frame = ProtocolFrame(
            version = ProtocolVersion.Current,
            type = FrameType.DATA,
            payload = payload,
        )
        connection.send(ProtocolFrameCodec.encode(frame))
    }
}
