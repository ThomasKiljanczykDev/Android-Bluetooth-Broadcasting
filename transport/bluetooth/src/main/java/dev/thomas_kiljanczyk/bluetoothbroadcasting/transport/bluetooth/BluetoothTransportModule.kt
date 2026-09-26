package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.bluetooth

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastClient
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerDiscovery
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportRequirements

@Module
@InstallIn(ViewModelComponent::class)
abstract class BluetoothTransportModule {

    @Binds
    @ViewModelScoped
    abstract fun bindServer(impl: BluetoothBroadcastServer): BroadcastServer

    @Binds
    @ViewModelScoped
    abstract fun bindClient(impl: BluetoothBroadcastClient): BroadcastClient

    @Binds
    @ViewModelScoped
    abstract fun bindDiscovery(impl: BluetoothServerDiscovery): ServerDiscovery
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BluetoothRequirementsModule {

    @Binds
    abstract fun bindRequirements(impl: BluetoothTransportRequirements): TransportRequirements
}
