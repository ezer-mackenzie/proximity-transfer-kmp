package io.github.ezer_mackenzie.proximitytransfer.core.engine

import io.github.ezer_mackenzie.proximitytransfer.core.bootstrap.BootstrapPayload
import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.MultiItemManifest
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume.ChunkBitmap
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume.ResumeRequest
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/**
 * Unified high-level interface for proximity peer-to-peer data transfers.
 */
interface ProximityTransferEngine {

    /** Generates a [BootstrapPayload] for this peer listening on [host] and [port]. */
    fun createBootstrapPayload(
        peerId: String,
        host: String,
        port: Int,
        targetCapability: TransportCapability = TransportCapability.LOCAL_NETWORK,
        bootstrapCapability: TransportCapability = TransportCapability.QR_BOOTSTRAP,
    ): BootstrapPayload

    /** Formats [payload] as a QR code URI string (`proximity://v1?data=...`). */
    fun formatQrUri(payload: BootstrapPayload): String

    /** Parses a QR code URI string back into a [BootstrapPayload]. */
    fun parseQrUri(uriString: String): BootstrapPayload

    /** Formats [payload] as a binary NDEF message for NFC transfer. */
    fun formatNdefBytes(payload: BootstrapPayload): ByteArray

    /** Parses binary NDEF bytes back into a [BootstrapPayload]. */
    fun parseNdefBytes(ndefBytes: ByteArray): BootstrapPayload

    /**
     * Sends [payload] bytes over an established [connection].
     *
     * @param connection Open connection to the remote peer.
     * @param payload Payload bytes to transmit.
     * @param session Optional [TransferSession] to track state transitions.
     * @param resumeRequest Optional [ResumeRequest] for selective chunk re-transmission.
     */
    suspend fun sendPayload(
        connection: Connection,
        payload: ByteArray,
        session: TransferSession = TransferSession(),
        resumeRequest: ResumeRequest? = null,
        keySpec: io.github.ezer_mackenzie.proximitytransfer.core.security.SessionKeySpec? = null,
    )

    /**
     * Receives and verifies payload bytes over an established [connection].
     *
     * @param connection Open connection to the remote peer.
     * @param session Optional [TransferSession] to track state transitions.
     * @param existingBitmap Optional [ChunkBitmap] to resume an interrupted session.
     * @param keySpec Optional [SessionKeySpec] for frame payload encryption/decryption.
     * @return Pair containing reconstructed payload bytes and the updated [ChunkBitmap].
     */
    suspend fun receivePayload(
        connection: Connection,
        session: TransferSession = TransferSession(),
        existingBitmap: ChunkBitmap? = null,
        keySpec: io.github.ezer_mackenzie.proximitytransfer.core.security.SessionKeySpec? = null,
    ): Pair<ByteArray, ChunkBitmap>

    /** Encodes a multi-file manifest into binary format for transmission. */
    fun encodeMultiItemManifest(manifest: MultiItemManifest): ByteArray

    /** Decodes binary manifest bytes into a [MultiItemManifest]. */
    fun decodeMultiItemManifest(bytes: ByteArray): MultiItemManifest

    companion object {
        /** Creates a new instance of [ProximityTransferEngine]. */
        fun create(): ProximityTransferEngine = DefaultProximityTransferEngine()
    }
}
