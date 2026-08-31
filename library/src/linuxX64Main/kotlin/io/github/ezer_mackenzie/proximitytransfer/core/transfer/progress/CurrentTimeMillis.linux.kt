package io.github.ezer_mackenzie.proximitytransfer.core.transfer.progress

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.time

@OptIn(ExperimentalForeignApi::class)
internal actual fun getCurrentTimeMillis(): Long = platform.posix.time(null) * 1000L
