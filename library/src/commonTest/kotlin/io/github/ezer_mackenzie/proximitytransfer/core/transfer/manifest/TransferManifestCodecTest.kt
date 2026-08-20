package io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransferManifestCodecTest {
    @Test
    fun roundTripsManifest() {
        val manifest = TransferManifest(
            payloadSize = 65_536,
            chunkCount = 4,
            sha256 = Sha256.digest(byteArrayOf(1, 2, 3)),
        )

        assertEquals(manifest, TransferManifestCodec.decode(TransferManifestCodec.encode(manifest)))
    }

    @Test
    fun rejectsIncorrectEncodedSize() {
        assertFailsWith<TransferManifestDecodingException> {
            TransferManifestCodec.decode(ByteArray(TransferManifestCodec.ENCODED_SIZE - 1))
        }
    }

    @Test
    fun rejectsNonPositiveChunkCount() {
        val encoded = TransferManifestCodec.encode(
            TransferManifest(0, 1, Sha256.digest(byteArrayOf())),
        ).also { it[11] = 0 }

        assertFailsWith<TransferManifestDecodingException> {
            TransferManifestCodec.decode(encoded)
        }
    }
}
