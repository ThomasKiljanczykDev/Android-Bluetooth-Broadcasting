package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import javax.inject.Inject

internal val SERVICE_ID: String = TransportConstants.SERVICE_UUID.toString()

internal val STRATEGY: Strategy = Strategy.P2P_STAR

class LocalEndpointName @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val value: String
        get() = Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
            ?: Build.MODEL
}

internal object IgnoringPayloadCallback : PayloadCallback() {
    override fun onPayloadReceived(endpointId: String, payload: Payload) {}
    override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
}

internal fun CoroutineScope.onCancellation(block: () -> Unit) {
    launch {
        try {
            awaitCancellation()
        } finally {
            block()
        }
    }
}
