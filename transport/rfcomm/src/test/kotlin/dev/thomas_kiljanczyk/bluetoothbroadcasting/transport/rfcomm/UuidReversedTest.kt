package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.rfcomm

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class UuidReversedTest {

    @Test
    fun `reverses all 16 bytes`() {
        val uuid = UUID.fromString("00112233-4455-6677-8899-aabbccddeeff")

        assertEquals(UUID.fromString("ffeeddcc-bbaa-9988-7766-554433221100"), uuid.reversed())
    }

    @Test
    fun `is an involution`() {
        assertEquals(SERVICE_UUID, SERVICE_UUID.reversed().reversed())
    }
}
