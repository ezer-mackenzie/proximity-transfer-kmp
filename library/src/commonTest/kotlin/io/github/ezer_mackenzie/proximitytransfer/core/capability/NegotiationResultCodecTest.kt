package io.github.ezer_mackenzie.proximitytransfer.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NegotiationResultCodecTest {
    @Test
    fun roundTripsSelectionWithBootstrap() {
        val expected = NegotiationResult(
            dataTransport = TransportCapability.LOCAL_NETWORK,
            bootstrapTransport = TransportCapability.QR_BOOTSTRAP,
        )

        assertEquals(expected, NegotiationResultCodec.decode(NegotiationResultCodec.encode(expected)))
    }

    @Test
    fun roundTripsSelectionWithoutBootstrap() {
        val expected = NegotiationResult(
            dataTransport = TransportCapability.BLE,
            bootstrapTransport = null,
        )

        assertEquals(expected, NegotiationResultCodec.decode(NegotiationResultCodec.encode(expected)))
    }

    @Test
    fun rejectsMalformedAndInvalidSelections() {
        assertFailsWith<NegotiationResultDecodingException> {
            NegotiationResultCodec.decode(byteArrayOf(TransportCapability.BLE.code.toByte()))
        }
        assertFailsWith<NegotiationResultDecodingException> {
            NegotiationResultCodec.decode(byteArrayOf(99, 0))
        }
        assertFailsWith<NegotiationResultDecodingException> {
            NegotiationResultCodec.decode(
                byteArrayOf(
                    TransportCapability.QR_BOOTSTRAP.code.toByte(),
                    0,
                ),
            )
        }
    }
}
