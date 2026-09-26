package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.rfcomm

import java.io.ByteArrayOutputStream

/**
 * Wire format: UTF-8 message + [TERMINATOR]; messages must not contain U+0004.
 * Decodes complete frames only, so multi-byte characters may span reads.
 */
internal class MessageFramer {
    companion object {
        const val TERMINATOR: Byte = 0x04

        fun encode(message: String): ByteArray = message.encodeToByteArray() + TERMINATOR
    }

    private val pending = ByteArrayOutputStream()

    fun decode(bytes: ByteArray, length: Int = bytes.size): List<String> {
        val messages = mutableListOf<String>()
        for (i in 0 until length) {
            val byte = bytes[i]
            if (byte == TERMINATOR) {
                messages += pending.toByteArray().decodeToString()
                pending.reset()
            } else {
                pending.write(byte.toInt())
            }
        }
        return messages
    }
}
