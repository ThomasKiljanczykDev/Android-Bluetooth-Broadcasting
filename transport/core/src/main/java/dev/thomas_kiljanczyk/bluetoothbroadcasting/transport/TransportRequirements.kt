package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport

import androidx.annotation.StringRes

enum class Radio { Bluetooth, WiFi, Location }

sealed interface Availability {
    data object Available : Availability
    data class Unavailable(@param:StringRes val reason: Int) : Availability
}

/** Must be satisfied before any [BroadcastServer], [BroadcastClient] or [ServerDiscovery] call. */
interface TransportRequirements {
    val runtimePermissions: Array<String>
    val requiredRadios: Set<Radio>

    fun checkAvailability(): Availability
}
