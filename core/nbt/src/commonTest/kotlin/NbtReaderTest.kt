package app.vercors.core.nbt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private fun MutableList<Byte>.writeTagId(id: Int) {
    add(id.toByte())
}

private fun MutableList<Byte>.writeUnsignedShort(value: Int) {
    add(((value shr 8) and 0xFF).toByte())
    add((value and 0xFF).toByte())
}

private fun MutableList<Byte>.writeInt(value: Int) {
    for (shift in intArrayOf(24, 16, 8, 0)) add(((value shr shift) and 0xFF).toByte())
}

private fun MutableList<Byte>.writeString(value: String) {
    val bytes = value.encodeToByteArray()
    writeUnsignedShort(bytes.size)
    bytes.forEach { add(it) }
}

private fun MutableList<Byte>.writeNamedString(name: String, value: String) {
    writeTagId(8) // TAG_String
    writeString(name)
    writeString(value)
}

class NbtReaderTest {
    @Test
    fun decodesServersDatShapedDocument() {
        val bytes = buildList {
            writeTagId(10) // root TAG_Compound
            writeString("") // root name, conventionally empty
            writeTagId(9) // "servers": TAG_List
            writeString("servers")
            writeTagId(10) // element type: TAG_Compound
            writeInt(1) // list length
            writeNamedString("name", "Test Server")
            writeNamedString("ip", "127.0.0.1:25565")
            writeTagId(0) // end of list element compound
            writeTagId(0) // end of root compound
        }.toByteArray()

        val root = readNbt(bytes)
        val servers = root.getList("servers")
        assertNotNull(servers)
        assertEquals(1, servers.value.size)
        val first = servers.value[0] as NbtCompound
        assertEquals("Test Server", first.getString("name"))
        assertEquals("127.0.0.1:25565", first.getString("ip"))
    }

    @Test
    fun decodesMultiByteIntegersAsBigEndian() {
        val bytes = buildList {
            writeTagId(10)
            writeString("")
            writeTagId(2) // TAG_Short
            writeString("value")
            add(0x01)
            add(0x00) // big-endian 0x0100 == 256; little-endian misread would yield 1
            writeTagId(0)
        }.toByteArray()

        val root = readNbt(bytes)
        assertEquals(256.toShort(), root.getShort("value"))
    }

    @Test
    fun skipsUnrelatedFieldsWhileWalkingACompound() {
        val bytes = buildList {
            writeTagId(10)
            writeString("")
            writeTagId(7) // TAG_Byte_Array, unrelated field that must be correctly skipped
            writeString("icon")
            writeInt(3)
            add(1); add(2); add(3)
            writeNamedString("name", "After Byte Array")
            writeTagId(0)
        }.toByteArray()

        val root = readNbt(bytes)
        assertEquals("After Byte Array", root.getString("name"))
    }
}
