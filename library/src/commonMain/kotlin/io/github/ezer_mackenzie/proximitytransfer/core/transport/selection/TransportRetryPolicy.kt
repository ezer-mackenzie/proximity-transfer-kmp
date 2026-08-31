package io.github.ezer_mackenzie.proximitytransfer.core.transport.selection

import kotlinx.coroutines.delay

/**
 * Configurable retry policy with exponential backoff for transport connection attempts.
 */
class TransportRetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelayMs: Long = 100L,
    val maxDelayMs: Long = 2000L,
    val backoffMultiplier: Double = 2.0,
) {
    init {
        require(maxAttempts > 0) { "maxAttempts must be greater than 0" }
        require(initialDelayMs >= 0) { "initialDelayMs cannot be negative" }
        require(maxDelayMs >= initialDelayMs) { "maxDelayMs must be greater than or equal to initialDelayMs" }
        require(backoffMultiplier >= 1.0) { "backoffMultiplier must be at least 1.0" }
    }

    /**
     * Executes [block] up to [maxAttempts] times, backing off exponentially on transient failure.
     */
    suspend fun <T> execute(block: suspend (attempt: Int) -> T): T {
        var currentDelay = initialDelayMs
        var lastException: Throwable? = null

        for (attempt in 1..maxAttempts) {
            try {
                return block(attempt)
            } catch (e: Throwable) {
                lastException = e
                if (attempt == maxAttempts) {
                    break
                }
                delay(currentDelay)
                currentDelay = (currentDelay * backoffMultiplier).toLong().coerceAtMost(maxDelayMs)
            }
        }
        throw lastException ?: IllegalStateException("Retry policy failed without capturing an exception")
    }
}
