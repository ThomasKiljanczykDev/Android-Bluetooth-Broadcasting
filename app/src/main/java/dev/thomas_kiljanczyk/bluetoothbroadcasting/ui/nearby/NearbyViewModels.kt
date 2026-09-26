package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.nearby

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby.NearbyBroadcastClient
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby.NearbyBroadcastServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby.NearbyServerDiscovery
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.ClientViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.pickserver.PickDeviceDialogViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server.ServerViewModel
import javax.inject.Inject

@HiltViewModel
class NearbyServerViewModel @Inject constructor(server: NearbyBroadcastServer) : ServerViewModel(server)

@HiltViewModel
class NearbyClientViewModel @Inject constructor(client: NearbyBroadcastClient) : ClientViewModel(client)

@HiltViewModel
class NearbyPickDeviceViewModel @Inject constructor(discovery: NearbyServerDiscovery) : PickDeviceDialogViewModel(discovery)
