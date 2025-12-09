package com.ridermesh.app.network

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import java.util.UUID

class BleRideAdvertiser(private val context: Context) {
    private val advertiser: BluetoothLeAdvertiser? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
        bluetoothManager?.adapter?.bluetoothLeAdvertiser
    }

    private var callback: AdvertiseCallback? = null

    fun startAdvertising(rideId: String, hostId: String) {
        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(UUID.fromString(RIDE_SERVICE_UUID)))
            .addServiceData(ParcelUuid(UUID.fromString(RIDE_SERVICE_UUID)), "$rideId|$hostId".encodeToBytes())
            .setIncludeDeviceName(false)
            .build()

        val settings = AdvertiseSettings.Builder()
            .setConnectable(true)
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .build()

        callback = object : AdvertiseCallback() {}
        advertiser?.startAdvertising(settings, data, callback)
    }

    fun stopAdvertising() {
        callback?.let { advertiser?.stopAdvertising(it) }
        callback = null
    }

    private fun String.encodeToBytes(): ByteArray = toByteArray(Charsets.UTF_8)

    companion object {
        const val RIDE_SERVICE_UUID = "4c44f7e6-9a6d-44b0-94e8-5026efa0bb1e"
    }
}
