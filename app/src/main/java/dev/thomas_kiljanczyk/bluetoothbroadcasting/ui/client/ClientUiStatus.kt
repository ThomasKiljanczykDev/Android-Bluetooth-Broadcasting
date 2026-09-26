package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client

sealed class ClientUiStatus {
    object Disconnected : ClientUiStatus()
    object Connecting : ClientUiStatus()
    object ConnectionFailed : ClientUiStatus()
    object ConnectedUnknown : ClientUiStatus()
    data class Connected(val deviceName: String) : ClientUiStatus()
    data class Listening(val serverCount: Int) : ClientUiStatus()
}
