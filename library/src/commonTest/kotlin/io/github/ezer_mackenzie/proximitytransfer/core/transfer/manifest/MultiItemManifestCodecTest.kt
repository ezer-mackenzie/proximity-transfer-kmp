package io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MultiItemManifestCodecTest {

    private val item1 = ManifestItem(
        id = "item-1",
        relativePath = "docs/readme.txt",
        size = 1024L,
        sha256 = Sha256.digest("Hello World".encodeToByteArray()),
    )

    private val item2 = ManifestItem(
        id = "item-2",
        relativePath = "images/logo.png",
        size = 20480L,
        sha256 = Sha256.digest("Fake Image Data".encodeToByteArray()),
    )

    private val sampleManifest = MultiItemManifest(
        items = listOf(item1, item2),
        overallSha256 = Sha256.digest("Overall Payload Bytes".encodeToByteArray()),
    )

    @Test
    fun encodeAndDecodeMultiItemManifestRoundTrip() {
        val encoded = MultiItemManifestCodec.encode(sampleManifest)
        val decoded = MultiItemManifestCodec.decode(encoded)

        assertEquals(sampleManifest.items.size, decoded.items.size)
        assertEquals(sampleManifest.items[0], decoded.items[0])
        assertEquals(sampleManifest.items[1], decoded.items[1])
        assertEquals(sampleManifest.totalSize, decoded.totalSize)
        assertEquals(sampleManifest.overallSha256.toList(), decoded.overallSha256.toList())
    }

    @Test
    fun failsOnTruncatedHeader() {
        assertFailsWith<TransferManifestDecodingException> {
            MultiItemManifestCodec.decode(byteArrayOf(0x01, 0x00))
        }
    }

    @Test
    fun failsOnInvalidVersion() {
        val encoded = MultiItemManifestCodec.encode(sampleManifest)
        encoded[0] = 0x99.toByte()

        assertFailsWith<TransferManifestDecodingException> {
            MultiItemManifestCodec.decode(encoded)
        }
    }

    @Test
    fun failsOnZeroItemCount() {
        val bytes = byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x00) + ByteArray(32)
        assertFailsWith<TransferManifestDecodingException> {
            MultiItemManifestCodec.decode(bytes)
        }
    }

    @Test
    fun validatesDuplicateItemIds() {
        val dupItem = ManifestItem(
            id = "item-1",
            relativePath = "docs/other.txt",
            size = 50L,
            sha256 = Sha256.digest("other".encodeToByteArray()),
        )
        assertFailsWith<IllegalArgumentException> {
            MultiItemManifest(listOf(item1, dupItem), Sha256.digest("test".encodeToByteArray()))
        }
    }
}
