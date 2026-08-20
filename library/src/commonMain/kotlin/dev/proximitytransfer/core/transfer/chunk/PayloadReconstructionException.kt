package dev.proximitytransfer.core.transfer.chunk

/** Indicates that a received set of chunks cannot form one complete payload. */
class PayloadReconstructionException(message: String) : IllegalArgumentException(message)
