package io.github.ezer_mackenzie.proximitytransfer.core.bootstrap

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** Formats and parses QR code URI strings for [BootstrapPayload]. */
object QrBootstrapCodec {
    private const val URI_SCHEME = "proximity://v1?data="

    @OptIn(ExperimentalEncodingApi::class)
    fun encodeToUri(payload: BootstrapPayload): String {
        val binaryBytes = BootstrapPayloadCodec.encode(payload)
        val base64Data = Base64.UrlSafe.encode(binaryBytes)
        return "$URI_SCHEME$base64Data"
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decodeFromUri(uriString: String): BootstrapPayload {
        if (!uriString.startsWith(URI_SCHEME)) {
            throw BootstrapCodecException("Invalid QR URI scheme: expected to start with '$URI_SCHEME'")
        }
        val base64Data = uriString.substring(URI_SCHEME.length)
        if (base64Data.isBlank()) {
            throw BootstrapCodecException("QR URI data payload cannot be blank")
        }
        try {
            val binaryBytes = Base64.UrlSafe.decode(base64Data)
            return BootstrapPayloadCodec.decode(binaryBytes)
        } catch (e: Exception) {
            if (e is BootstrapCodecException) throw e
            throw BootstrapCodecException("Failed to decode Base64 data from QR URI: ${e.message}", e)
        }
    }
}
