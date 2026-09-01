package io.github.ezer_mackenzie.proximitytransfer.core.engine

import io.github.ezer_mackenzie.proximitytransfer.core.bootstrap.BootstrapPayload
import io.github.ezer_mackenzie.proximitytransfer.core.bootstrap.NfcBootstrapCodec
import io.github.ezer_mackenzie.proximitytransfer.core.bootstrap.QrBootstrapCodec
import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.ResumableProtocolReceiver
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.ResumableProtocolSender
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.MultiItemManifest
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.MultiItemManifestCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume.ChunkBitmap
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume.ResumeRequest
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection
import kotlin.random.Random

/** Default multiplatform implementation of [ProximityTransferEngine]. */
class DefaultProximityTransferEngine : ProximityTransferEngine {

    override fun createBootstrapPayload(
        peerId: String,
        host: String,
        port: Int,
        targetCapability: TransportCapability,
        bootstrapCapability: TransportCapability,
    ): BootstrapPayload {
        val sessionToken = Random.nextBytes(16)
        return BootstrapPayload(
            peerId = peerId,
            bootstrapCapability = bootstrapCapability,
            targetCapability = targetCapability,
            host = host,
            port = port,
            sessionToken = sessionToken,
        )
    }

    override fun formatQrUri(payload: BootstrapPayload): String {
        return QrBootstrapCodec.encodeToUri(payload)
    }

    override fun parseQrUri(uriString: String): BootstrapPayload {
        return QrBootstrapCodec.decodeFromUri(uriString)
    }

    override fun formatNdefBytes(payload: BootstrapPayload): ByteArray {
        return NfcBootstrapCodec.encodeToNdef(payload)
    }

    override fun parseNdefBytes(ndefBytes: ByteArray): BootstrapPayload {
        return NfcBootstrapCodec.decodeFromNdef(ndefBytes)
    }

    override suspend fun sendPayload(
        connection: Connection,
        payload: ByteArray,
        session: TransferSession,
        resumeRequest: ResumeRequest?,
        keySpec: io.github.ezer_mackenzie.proximitytransfer.core.security.SessionKeySpec?,
    ) {
        val sender = ResumableProtocolSender(connection = connection, session = session, keySpec = keySpec)
        sender.send(payload, resumeRequest)
    }

    override suspend fun receivePayload(
        connection: Connection,
        session: TransferSession,
        existingBitmap: ChunkBitmap?,
        keySpec: io.github.ezer_mackenzie.proximitytransfer.core.security.SessionKeySpec?,
    ): Pair<ByteArray, ChunkBitmap> {
        val receiver = ResumableProtocolReceiver(connection = connection, session = session, keySpec = keySpec)
        return receiver.receive(existingBitmap = existingBitmap)
    }

    override fun encodeMultiItemManifest(manifest: MultiItemManifest): ByteArray {
        return MultiItemManifestCodec.encode(manifest)
    }

    override fun decodeMultiItemManifest(bytes: ByteArray): MultiItemManifest {
        return MultiItemManifestCodec.decode(bytes)
    }
}
