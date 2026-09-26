package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import java.nio.ByteBuffer

/**
 * Service data layout: version(1) | sessionId(4) | seq(2) | nameLength(1) | name | message.
 * [seq] 0 means no message yet.
 */
internal data class AdvertPayload(
    val sessionId: Int,
    val seq: Int,
    val name: String,
    val message: String
) {
    fun encode(): ByteArray {
        val nameBytes = name.encodeToByteArray()
        require(nameBytes.size <= MAX_NAME_BYTES) { "Name exceeds $MAX_NAME_BYTES bytes" }
        val messageBytes = message.encodeToByteArray()
        return ByteBuffer.allocate(HEADER_BYTES + nameBytes.size + messageBytes.size)
            .put(VERSION)
            .putInt(sessionId)
            .putShort(seq.toShort())
            .put(nameBytes.size.toByte())
            .put(nameBytes)
            .put(messageBytes)
            .array()
    }

    companion object {
        const val VERSION: Byte = 1
        const val HEADER_BYTES = 8
        const val MAX_NAME_BYTES = 24
        const val MAX_SEQ = 0xFFFF

        /** Fits one AUX_ADV_IND PDU; chained (AUX_CHAIN_IND) data may arrive truncated. */
        const val MAX_ADVERTISING_DATA_BYTES = 200

        /** AD length + AD type + 128-bit service UUID. */
        const val SERVICE_DATA_OVERHEAD = 18

        fun maxMessageBytes(name: String): Int =
            MAX_ADVERTISING_DATA_BYTES - SERVICE_DATA_OVERHEAD - HEADER_BYTES -
                name.encodeToByteArray().size

        fun nextSeq(seq: Int): Int = if (seq >= MAX_SEQ) 1 else seq + 1

        fun decode(bytes: ByteArray): AdvertPayload? {
            if (bytes.size < HEADER_BYTES || bytes[0] != VERSION) return null
            val buffer = ByteBuffer.wrap(bytes, 1, HEADER_BYTES - 1)
            val sessionId = buffer.int
            val seq = buffer.short.toInt() and 0xFFFF
            val nameEnd = HEADER_BYTES + (buffer.get().toInt() and 0xFF)
            if (nameEnd > bytes.size) return null
            return AdvertPayload(
                sessionId = sessionId,
                seq = seq,
                name = bytes.decodeToString(HEADER_BYTES, nameEnd),
                message = bytes.decodeToString(nameEnd, bytes.size)
            )
        }
    }
}

internal fun sessionKey(sessionId: Int): String = sessionId.toUInt().toString(16).padStart(8, '0')

internal fun parseSessionKey(key: String): Int? = key.toUIntOrNull(16)?.toInt()

/** Longest prefix encoding to at most [maxBytes] UTF-8 bytes, cut at a code point boundary. */
internal fun String.truncateUtf8(maxBytes: Int): String {
    var bytes = 0
    var end = 0
    while (end < length) {
        val codePoint = codePointAt(end)
        val size = when {
            codePoint < 0x80 -> 1
            codePoint < 0x800 -> 2
            codePoint < 0x10000 -> 3
            else -> 4
        }
        if (bytes + size > maxBytes) break
        bytes += size
        end += Character.charCount(codePoint)
    }
    return substring(0, end)
}
