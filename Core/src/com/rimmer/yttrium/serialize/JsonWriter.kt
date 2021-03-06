package com.rimmer.yttrium.serialize

import com.rimmer.yttrium.ByteString
import com.rimmer.yttrium.ByteStringBuilder
import com.rimmer.yttrium.utf8
import io.netty.buffer.ByteBuf
import org.joda.time.DateTime

/** Utility functions for generating json data. */
class JsonWriter(val buffer: ByteBuf) {
    var depth = 0
    var hasValue = 0

    fun startObject(): JsonWriter {
        buffer.writeByte('{'.toInt())
        depth++
        return this
    }

    fun endObject(): JsonWriter {
        buffer.writeByte('}'.toInt())
        depth--
        hasValue = depth
        return this
    }

    fun startArray(): JsonWriter {
        buffer.writeByte('['.toInt())
        depth++
        return this
    }

    fun endArray(): JsonWriter {
        buffer.writeByte(']'.toInt())
        depth--
        hasValue = depth
        return this
    }

    fun arrayField(): JsonWriter {
        if(hasValue < depth) {
            hasValue = depth
        } else {
            buffer.writeByte(','.toInt())
        }
        return this
    }

    fun field(name: ByteArray): JsonWriter {
        if(hasValue < depth) {
            hasValue = depth
        } else {
            buffer.writeByte(','.toInt())
        }
        buffer.writeByte('"'.toInt())
        buffer.writeBytes(name)
        buffer.writeByte('"'.toInt())
        buffer.writeByte(':'.toInt())
        return this
    }

    fun field(name: String) = field(name.toByteArray())

    fun nullValue() = buffer.writeBytes(nullBytes)

    fun value(s: String): JsonWriter {
        buffer.writeByte('"'.toInt())
        val escaped = StringBuilder(s.length)
        for(c in s) {
            if(c == '"') {
                escaped.append("\\\"")
            } else if(c == '\\') {
                escaped.append("\\\\")
            } else if(c < 0x20.toChar()) {
                if (c == '\b') {
                    escaped.append("\\b")
                } else if (c == 0x0C.toChar()) {
                    escaped.append("\\f")
                } else if (c == '\n') {
                    escaped.append("\\n")
                } else if (c == '\r') {
                    escaped.append("\\r")
                } else if (c == '\t') {
                    escaped.append("\\t")
                }
            } else {
                escaped.append(c)
            }
        }
        buffer.writeBytes(escaped.toString().toByteArray())
        buffer.writeByte('"'.toInt())
        return this
    }

    fun value(s: ByteString): JsonWriter {
        buffer.writeByte('"'.toInt())
        val escaped = ByteStringBuilder(s.size)
        for(c in s) {
            if(c == '"'.toByte()) {
                escaped.append("\\\"".utf8)
            } else if(c == '\\'.toByte()) {
                escaped.append("\\\\".utf8)
            } else if(c < 0x20.toByte()) {
                if (c == '\b'.toByte()) {
                    escaped.append("\\b".utf8)
                } else if (c == 0x0C.toByte()) {
                    escaped.append("\\f".utf8)
                } else if (c == '\n'.toByte()) {
                    escaped.append("\\n".utf8)
                } else if (c == '\r'.toByte()) {
                    escaped.append("\\r".utf8)
                } else if (c == '\t'.toByte()) {
                    escaped.append("\\t".utf8)
                }
            } else {
                escaped.append(c)
            }
        }

        escaped.string.write(buffer)
        buffer.writeByte('"'.toInt())
        return this
    }

    fun value(i: Double): JsonWriter {
        buffer.writeBytes(i.toString().toByteArray())
        return this
    }

    fun value(i: Long): JsonWriter {
        buffer.writeBytes(i.toString().toByteArray())
        return this
    }

    fun value(i: DateTime) = value(i.millis)
    fun value(i: Float) = value(i.toDouble())
    fun value(i: Int) = value(i.toLong())
    fun value(i: Short) = value(i.toLong())
    fun value(i: Byte) = value(i.toLong())

    fun value(i: Boolean): JsonWriter {
        buffer.writeBytes(if(i) trueBytes else falseBytes)
        return this
    }

    companion object {
        val trueBytes = "true".toByteArray()
        val falseBytes = "false".toByteArray()
        val nullBytes = "null".toByteArray()
    }
}