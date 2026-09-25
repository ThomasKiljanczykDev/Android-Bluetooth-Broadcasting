package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.bluetooth

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageFramerTest {

    private val framer = MessageFramer()

    @Test
    fun `encode appends terminator`() {
        assertArrayEquals(byteArrayOf('h'.code.toByte(), 'i'.code.toByte(), 0x04), MessageFramer.encode("hi"))
    }

    @Test
    fun `decodes single complete frame`() {
        assertEquals(listOf("hello"), framer.decode(MessageFramer.encode("hello")))
    }

    @Test
    fun `holds partial frame until terminator arrives`() {
        val frame = MessageFramer.encode("hello")

        assertEquals(emptyList<String>(), framer.decode(frame.copyOfRange(0, 3)))
        assertEquals(listOf("hello"), framer.decode(frame.copyOfRange(3, frame.size)))
    }

    @Test
    fun `decodes multiple frames from one read`() {
        val bytes = MessageFramer.encode("one") + MessageFramer.encode("two") + MessageFramer.encode("")

        assertEquals(listOf("one", "two", ""), framer.decode(bytes))
    }

    @Test
    fun `keeps trailing partial frame after complete ones`() {
        val bytes = MessageFramer.encode("one") + "tw".encodeToByteArray()

        assertEquals(listOf("one"), framer.decode(bytes))
        assertEquals(listOf("two"), framer.decode(MessageFramer.encode("o")))
    }

    @Test
    fun `terminator as last byte of read completes frame`() {
        framer.decode("ab".encodeToByteArray())

        assertEquals(listOf("ab"), framer.decode(byteArrayOf(MessageFramer.TERMINATOR)))
    }

    @Test
    fun `multi-byte UTF-8 character split across reads`() {
        val frame = MessageFramer.encode("zażółć 🚀")
        val split = frame.indexOfFirst { it.toInt() and 0xC0 == 0xC0 } + 1

        assertEquals(emptyList<String>(), framer.decode(frame.copyOfRange(0, split)))
        assertEquals(listOf("zażółć 🚀"), framer.decode(frame.copyOfRange(split, frame.size)))
    }

    @Test
    fun `honours length argument`() {
        val buffer = MessageFramer.encode("abc") + MessageFramer.encode("ignored")

        assertEquals(listOf("abc"), framer.decode(buffer, length = 4))
    }
}
