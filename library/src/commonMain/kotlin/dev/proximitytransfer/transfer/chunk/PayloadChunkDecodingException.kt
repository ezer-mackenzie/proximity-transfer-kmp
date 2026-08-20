package dev.proximitytransfer.transfer.chunk

/** Indicates that received bytes do not contain valid chunk metadata. */
class PayloadChunkDecodingException(message: String) : IllegalArgumentException(message)
