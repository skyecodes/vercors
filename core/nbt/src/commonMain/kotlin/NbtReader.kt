package app.vercors.core.nbt

private const val TAG_END = 0
private const val TAG_BYTE = 1
private const val TAG_SHORT = 2
private const val TAG_INT = 3
private const val TAG_LONG = 4
private const val TAG_FLOAT = 5
private const val TAG_DOUBLE = 6
private const val TAG_BYTE_ARRAY = 7
private const val TAG_STRING = 8
private const val TAG_LIST = 9
private const val TAG_COMPOUND = 10
private const val TAG_INT_ARRAY = 11
private const val TAG_LONG_ARRAY = 12

fun readNbt(bytes: ByteArray): NbtCompound {
    val reader = NbtByteReader(bytes)
    val rootType = reader.readTagId()
    require(rootType == TAG_COMPOUND) { "Root NBT tag must be a compound (type $TAG_COMPOUND), was type $rootType" }
    reader.readNbtString() // root tag name, unused
    return reader.readCompoundPayload()
}

private class NbtByteReader(private val bytes: ByteArray) {
    private var pos = 0

    private fun nextByte(): Int {
        val value = bytes[pos].toInt() and 0xFF
        pos++
        return value
    }

    fun readTagId(): Int = nextByte()

    fun readByte(): Byte = bytes[pos++]

    fun readUnsignedShort(): Int = (nextByte() shl 8) or nextByte()

    fun readShort(): Short = readUnsignedShort().toShort()

    fun readInt(): Int {
        var result = 0
        repeat(4) { result = (result shl 8) or nextByte() }
        return result
    }

    fun readLong(): Long {
        var result = 0L
        repeat(8) { result = (result shl 8) or nextByte().toLong() }
        return result
    }

    fun readFloat(): Float = Float.fromBits(readInt())

    fun readDouble(): Double = Double.fromBits(readLong())

    fun readByteArray(length: Int): ByteArray {
        val result = bytes.copyOfRange(pos, pos + length)
        pos += length
        return result
    }

    // Modified UTF-8 decoded as plain UTF-8: identical for ASCII and nearly all real content
    fun readNbtString(): String = readByteArray(readUnsignedShort()).decodeToString()
}

private fun NbtByteReader.readTagPayload(typeId: Int): NbtTag = when (typeId) {
    TAG_BYTE -> NbtByte(readByte())
    TAG_SHORT -> NbtShort(readShort())
    TAG_INT -> NbtInt(readInt())
    TAG_LONG -> NbtLong(readLong())
    TAG_FLOAT -> NbtFloat(readFloat())
    TAG_DOUBLE -> NbtDouble(readDouble())
    TAG_BYTE_ARRAY -> NbtByteArray(readByteArray(readInt()))
    TAG_STRING -> NbtString(readNbtString())
    TAG_LIST -> readListPayload()
    TAG_COMPOUND -> readCompoundPayload()
    TAG_INT_ARRAY -> readIntArrayPayload()
    TAG_LONG_ARRAY -> readLongArrayPayload()
    else -> error("Unknown NBT tag type $typeId")
}

private fun NbtByteReader.readListPayload(): NbtList {
    val elementType = readTagId()
    val length = readInt()
    if (length <= 0) return NbtList(emptyList())
    return NbtList(List(length) { readTagPayload(elementType) })
}

private fun NbtByteReader.readCompoundPayload(): NbtCompound {
    val entries = LinkedHashMap<String, NbtTag>()
    while (true) {
        val typeId = readTagId()
        if (typeId == TAG_END) break
        val name = readNbtString()
        entries[name] = readTagPayload(typeId)
    }
    return NbtCompound(entries)
}

private fun NbtByteReader.readIntArrayPayload(): NbtIntArray {
    val length = readInt()
    return NbtIntArray(IntArray(length) { readInt() })
}

private fun NbtByteReader.readLongArrayPayload(): NbtLongArray {
    val length = readInt()
    return NbtLongArray(LongArray(length) { readLong() })
}
