package com.ridermesh.app.network

import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

class BleRideScanner(private val context: Context) {
    fun scan(): Flow<RideAdvertisement> = callbackFlow {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
        val scanner: BluetoothLeScanner? = bluetoothManager?.adapter?.bluetoothLeScanner

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(BleRideAdvertiser.RIDE_SERVICE_UUID)))
                .build()
        )
        val settings = ScanSettings.Builder()
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val payload = result.scanRecord?.getServiceData(ParcelUuid(UUID.fromString(BleRideAdvertiser.RIDE_SERVICE_UUID)))
                    ?.decodeToString()
                    ?.split("|")
                if (payload != null && payload.size >= 2) {
                    trySend(
                        RideAdvertisement(
                            rideId = payload[0],
                            rideName = "Unknown Ride",
                            hostName = payload[1],
                            signalStrength = result.rssi,
                            connectedPeers = 1
                        )
                    )
                }
            }
        }

        scanner?.startScan(filters, settings, callback)
        awaitClose { scanner?.stopScan(callback) }
    }
}

private fun ByteArray.decodeToString(): String = toString(Charsets.UTF_8)
