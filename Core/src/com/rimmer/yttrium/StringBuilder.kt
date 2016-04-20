package com.rimmer.yttrium

import java.util.*

class ByteStringBuilder {
    constructor(capacity: Int = 16) {
        value = ByteArray(capacity)
    }

    constructor(string: ByteString) : this(string.size + 16) {
        append(string)
    }

    override fun toString(): String {
        return String(value, 0, count)
    }

    private var value: ByteArray
    private var count: Int = 0

    val length: Int get() = count
    val string: ByteString get() = LocalByteString(value)
    val capacity: Int get() = value.size

    fun ensureCapacity(minimumCapacity: Int) {
        if(minimumCapacity > 0 && minimumCapacity - value.size > 0) {
            expandCapacity(minimumCapacity)
        }
    }

    private fun expandCapacity(minimumCapacity: Int) {
        var newCapacity = value.size * 2 + 2
        if(newCapacity - minimumCapacity < 0) {
            newCapacity = minimumCapacity
        }
        if(newCapacity < 0) {
            if(minimumCapacity < 0) throw OutOfMemoryError()
            newCapacity = Integer.MAX_VALUE
        }
        value = Arrays.copyOf(value, newCapacity)
    }

    fun trimToSize() {
        if(count < value.size) {
            value = Arrays.copyOf(value, count)
        }
    }

    fun charAt(index: Int): Byte {
        if(index < 0 || index >= count)
            throw StringIndexOutOfBoundsException(index)
        return value[index]
    }

    fun getChars(srcBegin: Int, srcEnd: Int, dst: ByteArray, dstBegin: Int) {
        if (srcBegin < 0)
            throw StringIndexOutOfBoundsException(srcBegin)
        if (srcEnd < 0 || srcEnd > count)
            throw StringIndexOutOfBoundsException(srcEnd)
        if (srcBegin > srcEnd)
            throw StringIndexOutOfBoundsException("srcBegin > srcEnd")
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin)
    }

    fun setCharAt(index: Int, ch: Byte) {
        if(index < 0 || index >= count)
            throw StringIndexOutOfBoundsException(index)
        value[index] = ch
    }

    fun append(obj: Any?): ByteStringBuilder = append(obj.toString())
    fun append(s: CharSequence, start: Int, end: Int): ByteStringBuilder = append(s, start, end)

    fun append(str: ByteString): ByteStringBuilder {
        val len = str.size
        ensureCapacity(count + len)

        for(b in str) {
            appendFast(b)
        }

        count += len
        return this
    }

    fun append(asb: ByteStringBuilder): ByteStringBuilder {
        val len = asb.length
        ensureCapacity(count + len)
        asb.getChars(0, len, value, count)
        count += len
        return this
    }

    fun append(s: ByteString, start: Int, end: Int): ByteStringBuilder {
        if(start < 0 || start > end || end > s.size) {
            throw IndexOutOfBoundsException("start $start end $end size ${s.size}")
        }

        val len = end - start
        ensureCapacity(count + len)

        var i = start
        var j = count
        while (i < end) {
            value[j] = s[i]
            i++
            j++
        }

        count += len
        return this
    }

    fun append(str: CharArray): ByteStringBuilder {
        val len = str.size
        ensureCapacity(count + len)
        System.arraycopy(str, 0, value, count, len)
        count += len
        return this
    }

    fun append(str: CharArray, offset: Int, len: Int): ByteStringBuilder {
        ensureCapacity(count + len)
        System.arraycopy(str, offset, value, count, len)
        count += len
        return this
    }

    fun append(b: Boolean): ByteStringBuilder {
        if(b) {
            ensureCapacity(count + 4)
            value[count++] = 't'.toByte()
            value[count++] = 'r'.toByte()
            value[count++] = 'u'.toByte()
            value[count++] = 'e'.toByte()
        } else {
            ensureCapacity(count + 5)
            value[count++] = 'f'.toByte()
            value[count++] = 'a'.toByte()
            value[count++] = 'l'.toByte()
            value[count++] = 's'.toByte()
            value[count++] = 'e'.toByte()
        }
        return this
    }

    fun append(c: Byte): ByteStringBuilder {
        ensureCapacity(count + 1)
        value[count++] = c
        return this
    }

    private fun appendFast(c: Byte): ByteStringBuilder {
        value[count++] = c
        return this
    }

    fun delete(start: Int, end: Int): ByteStringBuilder {
        var end = end
        if(start < 0) throw StringIndexOutOfBoundsException(start)
        if(end > count) end = count
        if(start > end) throw StringIndexOutOfBoundsException()

        val len = end - start
        if(len > 0) {
            System.arraycopy(value, start + len, value, start, count - end)
            count -= len
        }
        return this
    }

    fun deleteCharAt(index: Int): ByteStringBuilder {
        if(index < 0 || index >= count)
            throw StringIndexOutOfBoundsException(index)
        System.arraycopy(value, index + 1, value, index, count - index - 1)
        count--
        return this
    }

    fun substring(start: Int): ByteString {
        return substring(start, count)
    }

    fun subSequence(start: Int, end: Int): ByteString {
        return substring(start, end)
    }

    fun substring(start: Int, end: Int): ByteString {
        if(start < 0)
            throw StringIndexOutOfBoundsException(start)
        if(end > count)
            throw StringIndexOutOfBoundsException(end)
        if(start > end)
            throw StringIndexOutOfBoundsException(end - start)

        return LocalByteString(value.copyOfRange(start, end-start))
    }

    fun insert(index: Int, str: CharArray, offset: Int, len: Int): ByteStringBuilder {
        if(index < 0 || index > length)
            throw StringIndexOutOfBoundsException(index)
        if(offset < 0 || len < 0 || offset > str.size - len)
            throw StringIndexOutOfBoundsException("offset $offset, len $len, str.length ${str.size}")

        ensureCapacity(count + len)
        System.arraycopy(value, index, value, index + len, count - index)
        System.arraycopy(str, offset, value, index, len)

        count += len
        return this
    }

    fun insert(offset: Int, obj: Any): ByteStringBuilder {
        return insert(offset, obj.toString())
    }

    fun insert(offset: Int, str: CharArray): ByteStringBuilder {
        if(offset < 0 || offset > length) throw StringIndexOutOfBoundsException(offset)
        val len = str.size

        ensureCapacity(count + len)
        System.arraycopy(value, offset, value, offset + len, count - offset)
        System.arraycopy(str, 0, value, offset, len)

        count += len
        return this
    }

    fun insert(dstOffset: Int, s: ByteString): ByteStringBuilder {
        return insert(dstOffset, s, 0, s.size)
    }

    fun insert(dstOffset: Int, s: ByteString, start: Int, end: Int): ByteStringBuilder {
        var dstOffset = dstOffset
        if(dstOffset < 0 || dstOffset > this.length)
            throw IndexOutOfBoundsException("dstOffset $dstOffset")
        if(start < 0 || end < 0 || start > end || end > s.size)
            throw IndexOutOfBoundsException("start $start, end $end, s.length() ${s.size}")

        val len = end - start
        ensureCapacity(count + len)
        System.arraycopy(value, dstOffset, value, dstOffset + len, count - dstOffset)

        for(i in start..end - 1) value[dstOffset++] = s[i]

        count += len
        return this
    }

    fun insert(offset: Int, b: Boolean): ByteStringBuilder {
        return insert(offset, b.toString())
    }

    fun insert(offset: Int, c: Byte): ByteStringBuilder {
        ensureCapacity(count + 1)
        System.arraycopy(value, offset, value, offset + 1, count - offset)

        value[offset] = c
        count += 1
        return this
    }

    fun insert(offset: Int, i: Int): ByteStringBuilder {
        return insert(offset, i.toString())
    }

    fun insert(offset: Int, l: Long): ByteStringBuilder {
        return insert(offset, l.toString())
    }

    fun insert(offset: Int, f: Float): ByteStringBuilder {
        return insert(offset, f.toString())
    }

    fun insert(offset: Int, d: Double): ByteStringBuilder {
        return insert(offset, d.toString())
    }
}
