package io.github.ezer_mackenzie.proximitytransfer.core.capability.codec

/** Indicates that received bytes do not describe a valid capability set. */
class DeviceCapabilitiesDecodingException(message: String) : IllegalArgumentException(message)
