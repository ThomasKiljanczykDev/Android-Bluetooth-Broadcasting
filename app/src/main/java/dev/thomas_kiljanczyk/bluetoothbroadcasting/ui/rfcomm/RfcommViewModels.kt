package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.rfcomm

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.rfcomm.RfcommBroadcastClient
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.rfcomm.RfcommBroadcastServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.rfcomm.RfcommServerDiscovery
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.ClientViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.pickserver.PickDeviceDialogViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server.ServerViewModel
import javax.inject.Inject

@HiltViewModel
class RfcommServerViewModel @Inject constructor(server: RfcommBroadcastServer) : ServerViewModel(server)

@HiltViewModel
class RfcommClientViewModel @Inject constructor(client: RfcommBroadcastClient) : ClientViewModel(client)

@HiltViewModel
class RfcommPickDeviceViewModel @Inject constructor(discovery: RfcommServerDiscovery) : PickDeviceDialogViewModel(discovery)
