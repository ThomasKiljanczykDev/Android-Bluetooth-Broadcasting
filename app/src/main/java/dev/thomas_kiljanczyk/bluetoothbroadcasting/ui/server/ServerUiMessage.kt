package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server

sealed class ServerUiMessage {
    object None : ServerUiMessage()
    object ConnectedUnknown : ServerUiMessage()
    data class Connected(val deviceName: String) : ServerUiMessage()
    object DisconnectedUnknown : ServerUiMessage()
    data class Disconnected(val deviceName: String) : ServerUiMessage()
}
