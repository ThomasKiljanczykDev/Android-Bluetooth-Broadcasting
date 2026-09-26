package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdvertPayloadTest {

    @Test
    fun `round-trips`() {
        val payload = AdvertPayload(sessionId = -123456, seq = 0xFFFF, name = "Pixel ✓", message = "héllo 👋")
        assertEquals(payload, AdvertPayload.decode(payload.encode()))
    }

    @Test
    fun `round-trips empty name and message`() {
        val payload = AdvertPayload(sessionId = 7, seq = 0, name = "", message = "")
        assertEquals(AdvertPayload.HEADER_BYTES, payload.encode().size)
        assertEquals(payload, AdvertPayload.decode(payload.encode()))
    }

    @Test
    fun `rejects unknown version`() {
        val bytes = AdvertPayload(1, 1, "a", "b").encode().also { it[0] = 2 }
        assertNull(AdvertPayload.decode(bytes))
    }

    @Test
    fun `rejects short buffer`() {
        assertNull(AdvertPayload.decode(ByteArray(AdvertPayload.HEADER_BYTES - 1) { 1 }))
    }

    @Test
    fun `rejects name length past end`() {
        val bytes = AdvertPayload(1, 1, "abc", "").encode()
        assertNull(AdvertPayload.decode(bytes.copyOf(bytes.size - 1)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects oversized name`() {
        AdvertPayload(1, 1, "a".repeat(AdvertPayload.MAX_NAME_BYTES + 1), "").encode()
    }

    @Test
    fun `max message fills advertising data budget`() {
        val name = "a".repeat(AdvertPayload.MAX_NAME_BYTES)
        val message = "b".repeat(AdvertPayload.maxMessageBytes(name))
        val size = AdvertPayload(1, 1, name, message).encode().size + AdvertPayload.SERVICE_DATA_OVERHEAD
        assertEquals(AdvertPayload.MAX_ADVERTISING_DATA_BYTES, size)
    }

    @Test
    fun `seq wraps to 1`() {
        assertEquals(1, AdvertPayload.nextSeq(0))
        assertEquals(1, AdvertPayload.nextSeq(AdvertPayload.MAX_SEQ))
    }

    @Test
    fun `session key is 8 hex digits`() {
        assertEquals("00000000", sessionKey(0))
        assertEquals("0000002a", sessionKey(42))
        assertEquals("ffffffff", sessionKey(-1))
        assertEquals("80000000", sessionKey(Int.MIN_VALUE))
    }

    @Test
    fun `truncates at code point boundary`() {
        assertEquals("abc", "abc".truncateUtf8(3))
        assertEquals("ab", "abc".truncateUtf8(2))
        assertEquals("a", "aé".truncateUtf8(2))
        assertEquals("a", "a👋".truncateUtf8(4))
        assertEquals("a👋", "a👋".truncateUtf8(5))
    }
}
