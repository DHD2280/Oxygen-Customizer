package it.dhd.oxygencustomizer.xmorph.engine.render

import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream

internal object FontAxes {

    private const val TABLE_COUNT_SKIP = 4
    private const val HEADER_REST = 6
    private const val TABLE_DIRECTORY = 12
    private const val TABLE_RECORD = 16
    private const val FVAR = "fvar"
    private const val FVAR_HEADER = 16
    private const val AXES_OFFSET = 4
    private const val AXIS_COUNT = 8
    private const val AXIS_SIZE = 10
    private const val MAX_FVAR = 64 * 1024
    private const val WEIGHT = "wght"

    fun hasWeight(stream: InputStream): Boolean = WEIGHT in axes(stream)

    fun axes(stream: InputStream): Set<String> = try {
        read(DataInputStream(stream.buffered()))
    } catch (_: IOException) {
        emptySet()
    }

    private fun read(input: DataInputStream): Set<String> {
        input.skipFully(TABLE_COUNT_SKIP)
        val tables = input.readUnsignedShort()
        input.skipFully(HEADER_REST)
        var fvarOffset = -1L
        var fvarLength = 0
        repeat(tables) {
            val name = input.readTag()
            input.skipFully(4)
            val offset = input.readInt().toLong() and 0xFFFFFFFFL
            val length = input.readInt()
            if (name == FVAR) {
                fvarOffset = offset
                fvarLength = length
            }
        }
        if (fvarOffset < 0 || fvarLength < FVAR_HEADER || fvarLength > MAX_FVAR) return emptySet()
        val read = TABLE_DIRECTORY + tables.toLong() * TABLE_RECORD
        if (fvarOffset < read) return emptySet()
        input.skipFully(fvarOffset - read)
        val fvar = ByteArray(fvarLength)
        input.readFully(fvar)
        val axesStart = u16(fvar, AXES_OFFSET)
        val count = u16(fvar, AXIS_COUNT)
        val size = u16(fvar, AXIS_SIZE)
        val found = LinkedHashSet<String>()
        for (axis in 0 until count) {
            val at = axesStart + axis * size
            if (at + 4 > fvar.size) break
            found += tag(fvar, at)
        }
        return found
    }

    private fun DataInputStream.readTag(): String =
        String(CharArray(4) { readUnsignedByte().toChar() })

    private fun DataInputStream.skipFully(count: Int) = skipFully(count.toLong())

    private fun DataInputStream.skipFully(count: Long) {
        var left = count
        while (left > 0) {
            val skipped = skip(left)
            if (skipped <= 0) {
                read().takeIf { it >= 0 } ?: throw IOException()
                left--
            } else {
                left -= skipped
            }
        }
    }

    private fun tag(bytes: ByteArray, at: Int): String =
        String(CharArray(4) { (bytes[at + it].toInt() and 0xFF).toChar() })

    private fun u16(bytes: ByteArray, at: Int): Int =
        ((bytes[at].toInt() and 0xFF) shl 8) or (bytes[at + 1].toInt() and 0xFF)
}
