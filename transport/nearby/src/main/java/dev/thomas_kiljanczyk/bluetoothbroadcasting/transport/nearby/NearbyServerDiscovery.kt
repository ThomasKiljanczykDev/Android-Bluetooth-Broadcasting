package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby

import android.util.Log
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerDiscovery
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class NearbyServerDiscovery @Inject constructor(
    private val connectionsClient: ConnectionsClient
) : ServerDiscovery {
    private companion object {
        const val TAG = "NearbyServerDiscovery"
    }

    override val emptyHint: Int = R.string.transport_nearby_discovery_hint

    override fun discover(): Flow<List<RemoteDevice>> = callbackFlow {
        val found = ConcurrentHashMap<String, RemoteDevice>()

        val callback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                found[endpointId] = RemoteDevice(endpointId, info.endpointName)
                trySend(found.values.toList())
            }

            override fun onEndpointLost(endpointId: String) {
                found.remove(endpointId)
                trySend(found.values.toList())
            }
        }

        connectionsClient.startDiscovery(
            SERVICE_ID,
            callback,
            DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        ).addOnFailureListener { e -> Log.e(TAG, "Failed to start discovery", e) }

        awaitClose { connectionsClient.stopDiscovery() }
    }
}
