package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object NearbyTransportModule {

    @Provides
    fun provideConnectionsClient(@ApplicationContext context: Context): ConnectionsClient =
        Nearby.getConnectionsClient(context)
}
