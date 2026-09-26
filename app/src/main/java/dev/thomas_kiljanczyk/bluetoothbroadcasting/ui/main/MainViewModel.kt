package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble.BleTransportRequirements
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby.NearbyTransportRequirements
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.rfcomm.RfcommTransportRequirements
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val ble: BleTransportRequirements,
    val rfcomm: RfcommTransportRequirements,
    val nearby: NearbyTransportRequirements
) : ViewModel()
