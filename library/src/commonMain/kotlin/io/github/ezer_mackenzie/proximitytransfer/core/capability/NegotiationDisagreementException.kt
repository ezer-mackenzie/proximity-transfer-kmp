package io.github.ezer_mackenzie.proximitytransfer.core.capability

/** Indicates that peers selected different routes from their shared capabilities. */
class NegotiationDisagreementException(
    val local: NegotiationResult,
    val remote: NegotiationResult,
) : IllegalStateException("Peers selected different transport routes: local=$local, remote=$remote")
