package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.Radio

/** [Radio.WiFi] relies on ACCESS_WIFI_STATE, declared by transports that require it. */
internal fun Radio.isEnabled(context: Context): Boolean = when (this) {
    Radio.Bluetooth -> context.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true
    Radio.WiFi -> context.getSystemService(WifiManager::class.java)?.isWifiEnabled == true
}

/** [Radio.Bluetooth] requires BLUETOOTH_CONNECT on API 31+. */
internal fun Radio.enableIntent(): Intent = when (this) {
    Radio.Bluetooth -> Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    Radio.WiFi -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Intent(Settings.Panel.ACTION_WIFI)
    } else {
        Intent(Settings.ACTION_WIFI_SETTINGS)
    }
}
