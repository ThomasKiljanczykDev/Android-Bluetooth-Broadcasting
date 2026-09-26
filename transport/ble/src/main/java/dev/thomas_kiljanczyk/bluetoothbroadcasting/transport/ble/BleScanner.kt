package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/** Permissions granted by the TransportRequirements gate before any call. */
@SuppressLint("MissingPermission")
class BleScanner @Inject constructor(
    private val adapterProvider: BleAdapterProvider
) {
    private companion object {
        const val TAG = "BleScanner"

        val FILTERS: List<ScanFilter> = listOf(
            ScanFilter.Builder().setServiceData(SERVICE_UUID, ByteArray(0)).build()
        )

        val SETTINGS: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setLegacy(false)
            .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
            .build()
    }

    /** Cold: scans while collected; completes if Bluetooth is off. > 5 starts per 30 s: no results. */
    internal fun adverts(): Flow<AdvertPayload> = callbackFlow {
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (result.dataStatus != ScanResult.DATA_COMPLETE) return
                val data = result.scanRecord?.getServiceData(SERVICE_UUID) ?: return
                AdvertPayload.decode(data)?.let { trySend(it) }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "Scan failed: $errorCode")
            }
        }

        val scanner = adapterProvider.adapterOrNull?.bluetoothLeScanner
        try {
            checkNotNull(scanner) { "Bluetooth off" }.startScan(FILTERS, SETTINGS, callback)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Scan not started", e)
            close()
            return@callbackFlow
        }

        awaitClose {
            try {
                scanner.stopScan(callback)
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Scan not stopped", e)
            }
        }
    }
}
