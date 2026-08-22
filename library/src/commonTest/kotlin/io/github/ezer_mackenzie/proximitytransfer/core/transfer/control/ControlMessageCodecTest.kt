package io.github.ezer_mackenzie.proximitytransfer.core.transfer.control

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ControlMessageCodecTest {
    @Test
    fun roundTripsCompletionAcknowledgement() {
        val digest = Sha256.digest(byteArrayOf(1, 2, 3))

        val decoded = CompletionAcknowledgementCodec.decode(
            CompletionAcknowledgementCodec.encode(CompletionAcknowledgement(digest)),
        )

        assertContentEquals(digest, decoded.sha256)
    }

    @Test
    fun rejectsInvalidCompletionDigestSize() {
        assertFailsWith<ControlMessageDecodingException> {
            CompletionAcknowledgementCodec.decode(ByteArray(Sha256.DIGEST_SIZE - 1))
        }
    }

    @Test
    fun roundTripsRemoteError() {
        val error = RemoteError(RemoteErrorCode.INTEGRITY_FAILURE, "Integrity failed")

        val decoded = RemoteErrorCodec.decode(RemoteErrorCodec.encode(error))

        assertEquals(error.code, decoded.code)
        assertEquals(error.message, decoded.message)
    }

    @Test
    fun rejectsUnknownRemoteErrorCode() {
        assertFailsWith<ControlMessageDecodingException> {
            RemoteErrorCodec.decode(byteArrayOf(127, 0, 0))
        }
    }

    @Test
    fun rejectsMismatchedRemoteErrorLength() {
        assertFailsWith<ControlMessageDecodingException> {
            RemoteErrorCodec.decode(byteArrayOf(1, 0, 1))
        }
    }
}
