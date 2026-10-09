package app.vercors.core.nbt

sealed interface NbtTag

data class NbtByte(val value: Byte) : NbtTag
data class NbtShort(val value: Short) : NbtTag
data class NbtInt(val value: Int) : NbtTag
data class NbtLong(val value: Long) : NbtTag
data class NbtFloat(val value: Float) : NbtTag
data class NbtDouble(val value: Double) : NbtTag
data class NbtString(val value: String) : NbtTag
data class NbtList(val value: List<NbtTag>) : NbtTag
data class NbtCompound(val value: Map<String, NbtTag>) : NbtTag

class NbtByteArray(val value: ByteArray) : NbtTag {
    override fun equals(other: Any?) = other is NbtByteArray && value.contentEquals(other.value)
    override fun hashCode() = value.contentHashCode()
}

class NbtIntArray(val value: IntArray) : NbtTag {
    override fun equals(other: Any?) = other is NbtIntArray && value.contentEquals(other.value)
    override fun hashCode() = value.contentHashCode()
}

class NbtLongArray(val value: LongArray) : NbtTag {
    override fun equals(other: Any?) = other is NbtLongArray && value.contentEquals(other.value)
    override fun hashCode() = value.contentHashCode()
}

fun NbtCompound.getString(key: String): String? = (value[key] as? NbtString)?.value
fun NbtCompound.getCompound(key: String): NbtCompound? = value[key] as? NbtCompound
fun NbtCompound.getList(key: String): NbtList? = value[key] as? NbtList
fun NbtCompound.getByte(key: String): Byte? = (value[key] as? NbtByte)?.value
fun NbtCompound.getShort(key: String): Short? = (value[key] as? NbtShort)?.value
fun NbtCompound.getInt(key: String): Int? = (value[key] as? NbtInt)?.value
fun NbtCompound.getLong(key: String): Long? = (value[key] as? NbtLong)?.value
