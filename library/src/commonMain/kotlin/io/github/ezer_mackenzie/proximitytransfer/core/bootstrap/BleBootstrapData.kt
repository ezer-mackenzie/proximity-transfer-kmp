package io.github.ezer_mackenzie.proximitytransfer.core.bootstrap

/**
 * Data class representing Bluetooth Low Energy bootstrap parameters.
 */
data class BleBootstrapData(
    val serviceUuidBytes: ByteArray,
    val txPower: Byte,
    val deviceName: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BleBootstrapData) return false
        if (!serviceUuidBytes.contentEquals(other.serviceUuidBytes)) return false
        if (txPower != other.txPower) return false
        if (deviceName != other.deviceName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = serviceUuidBytes.contentHashCode()
        result = 31 * result + txPower.hashCode()
        result = 31 * result + deviceName.hashCode()
        return result
    }
}
