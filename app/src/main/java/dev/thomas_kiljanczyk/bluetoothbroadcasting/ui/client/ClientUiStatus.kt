package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client

sealed class ClientUiStatus {
    object Disconnected : ClientUiStatus()
    object ConnectedUnknown : ClientUiStatus()
    data class Connected(val deviceName: String) : ClientUiStatus()
}
