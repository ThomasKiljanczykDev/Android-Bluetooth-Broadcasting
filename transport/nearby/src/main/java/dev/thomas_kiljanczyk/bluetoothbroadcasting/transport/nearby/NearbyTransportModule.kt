package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastClient
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerDiscovery
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportRequirements

@Module
@InstallIn(ViewModelComponent::class)
abstract class NearbyTransportModule {

    @Binds
    @ViewModelScoped
    abstract fun bindServer(impl: NearbyBroadcastServer): BroadcastServer

    @Binds
    @ViewModelScoped
    abstract fun bindClient(impl: NearbyBroadcastClient): BroadcastClient

    @Binds
    @ViewModelScoped
    abstract fun bindDiscovery(impl: NearbyServerDiscovery): ServerDiscovery

    companion object {
        @Provides
        fun provideConnectionsClient(@ApplicationContext context: Context): ConnectionsClient =
            Nearby.getConnectionsClient(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NearbyRequirementsModule {

    @Binds
    abstract fun bindRequirements(impl: NearbyTransportRequirements): TransportRequirements
}
