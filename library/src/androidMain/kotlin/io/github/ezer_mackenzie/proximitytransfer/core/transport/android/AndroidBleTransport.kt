package io.github.ezer_mackenzie.proximitytransfer.core.transport.android

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import io.github.ezer_mackenzie.proximitytransfer.core.transport.Transport
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/**
 * Android-specific Bluetooth Low Energy (BLE) transport foundation.
 * Bridges Android BLE GATT/L2CAP sockets to the common Connection interface.
 */
class AndroidBleTransport(
    val serviceUuid: String,
    val deviceAddress: String? = null,
) : Transport {

    override suspend fun open(): Connection {
        throw UnsupportedOperationException(
            "Android BLE hardware transport requires active BluetoothAdapter and GATT server/socket connection on device"
        )
    }
}
