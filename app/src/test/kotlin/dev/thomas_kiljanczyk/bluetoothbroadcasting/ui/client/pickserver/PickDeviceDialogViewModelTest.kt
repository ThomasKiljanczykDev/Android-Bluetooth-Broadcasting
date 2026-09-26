package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.pickserver

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.FakeServerDiscovery
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PickDeviceDialogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val discovery = FakeServerDiscovery(emptyHint = 42)
    private val viewModel by lazy { PickDeviceDialogViewModel(discovery) }

    @Test
    fun `discovers only while observed`() = runTest(mainDispatcherRule.dispatcher) {
        assertEquals(0, discovery.activeCollectors.value)

        val job = launch { viewModel.discoveredDevices.collect {} }
        assertEquals(1, discovery.activeCollectors.value)

        job.cancel()
        assertEquals(0, discovery.activeCollectors.value)
    }

    @Test
    fun `exposes discovered devices and resets after unsubscribe`() = runTest(mainDispatcherRule.dispatcher) {
        val device = RemoteDevice("id", "Server")
        val job = launch { viewModel.discoveredDevices.collect {} }

        discovery.devices.value = listOf(device)
        assertEquals(listOf(device), viewModel.discoveredDevices.value)

        job.cancel()
        assertEquals(emptyList<RemoteDevice>(), viewModel.discoveredDevices.value)
    }

    @Test
    fun `exposes transport hint`() {
        assertEquals(42, viewModel.emptyHint)
    }
}
