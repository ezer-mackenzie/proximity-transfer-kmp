package io.github.ezer_mackenzie.proximitytransfer.core.bootstrap

/**
 * Encodes and decodes [BootstrapPayload] to and from binary NDEF (NFC Data Exchange Format) message payloads.
 *
 * Formats data using standard NDEF Short Records (SR) with MIME type `application/vnd.proximitytransfer.bootstrap+bin`.
 */
object NfcBootstrapCodec {
    private const val MIME_TYPE = "application/vnd.proximitytransfer.bootstrap+bin"
    private const val TNF_MIME_MEDIA: Byte = 0x02
    private const val FLAG_MB: Byte = 0x80.toByte() // Message Begin
    private const val FLAG_ME: Byte = 0x40.toByte() // Message End
    private const val FLAG_SR: Byte = 0x10.toByte() // Short Record (1 byte payload length)

    /** Encodes [payload] into a single-record NDEF binary message. */
    fun encodeToNdef(payload: BootstrapPayload): ByteArray {
        val binaryPayload = BootstrapPayloadCodec.encode(payload)
        val mimeTypeBytes = MIME_TYPE.encodeToByteArray()

        require(mimeTypeBytes.size <= 0xFF) { "MIME type length exceeds 255 bytes" }

        val isShortRecord = binaryPayload.size <= 0xFF
        val headerFlags = (FLAG_MB.toInt() or FLAG_ME.toInt() or (if (isShortRecord) FLAG_SR.toInt() else 0) or TNF_MIME_MEDIA.toInt()).toByte()

        val typeLength = mimeTypeBytes.size.toByte()
        val payloadLengthSize = if (isShortRecord) 1 else 4
        val totalSize = 1 + 1 + payloadLengthSize + mimeTypeBytes.size + binaryPayload.size

        val buffer = ByteArray(totalSize)
        var offset = 0

        buffer[offset++] = headerFlags
        buffer[offset++] = typeLength

        if (isShortRecord) {
            buffer[offset++] = binaryPayload.size.toByte()
        } else {
            buffer[offset++] = (binaryPayload.size shr 24).toByte()
            buffer[offset++] = (binaryPayload.size shr 16).toByte()
            buffer[offset++] = (binaryPayload.size shr 8).toByte()
            buffer[offset++] = binaryPayload.size.toByte()
        }

        mimeTypeBytes.copyInto(buffer, offset)
        offset += mimeTypeBytes.size

        binaryPayload.copyInto(buffer, offset)

        return buffer
    }

    /** Decodes [ndefBytes] containing an NDEF message record into a [BootstrapPayload]. */
    fun decodeFromNdef(ndefBytes: ByteArray): BootstrapPayload {
        if (ndefBytes.size < 4) {
            throw BootstrapCodecException("NDEF payload too short")
        }

        var offset = 0
        val headerFlags = ndefBytes[offset++].toInt() and 0xFF
        val tnf = headerFlags and 0x07

        if (tnf != TNF_MIME_MEDIA.toInt() and 0xFF) {
            throw BootstrapCodecException("Invalid NDEF TNF: expected MIME Media ($TNF_MIME_MEDIA), found $tnf")
        }

        val typeLength = ndefBytes[offset++].toInt() and 0xFF
        val isShortRecord = (headerFlags and (FLAG_SR.toInt() and 0xFF)) != 0

        val payloadLength = if (isShortRecord) {
            if (offset >= ndefBytes.size) throw BootstrapCodecException("Unexpected EOF reading short payload length")
            ndefBytes[offset++].toInt() and 0xFF
        } else {
            if (offset + 4 > ndefBytes.size) throw BootstrapCodecException("Unexpected EOF reading 4-byte payload length")
            ((ndefBytes[offset++].toInt() and 0xFF) shl 24) or
                ((ndefBytes[offset++].toInt() and 0xFF) shl 16) or
                ((ndefBytes[offset++].toInt() and 0xFF) shl 8) or
                (ndefBytes[offset++].toInt() and 0xFF)
        }

        if (offset + typeLength + payloadLength > ndefBytes.size) {
            throw BootstrapCodecException("NDEF record length exceeds available data size")
        }

        val recordTypeBytes = ndefBytes.copyOfRange(offset, offset + typeLength)
        offset += typeLength

        val recordType = recordTypeBytes.decodeToString()
        if (recordType != MIME_TYPE) {
            throw BootstrapCodecException("Invalid NDEF MIME type: expected '$MIME_TYPE', found '$recordType'")
        }

        val binaryPayload = ndefBytes.copyOfRange(offset, offset + payloadLength)
        return BootstrapPayloadCodec.decode(binaryPayload)
    }
}
