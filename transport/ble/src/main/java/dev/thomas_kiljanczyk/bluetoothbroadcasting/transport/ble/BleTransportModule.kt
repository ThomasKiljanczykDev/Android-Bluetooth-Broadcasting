package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

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
abstract class BleTransportModule {

    @Binds
    @ViewModelScoped
    abstract fun bindServer(impl: BleBroadcastServer): BroadcastServer

    @Binds
    @ViewModelScoped
    abstract fun bindClient(impl: BleBroadcastClient): BroadcastClient

    @Binds
    @ViewModelScoped
    abstract fun bindDiscovery(impl: BleServerDiscovery): ServerDiscovery
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BleRequirementsModule {

    @Binds
    abstract fun bindRequirements(impl: BleTransportRequirements): TransportRequirements
}
