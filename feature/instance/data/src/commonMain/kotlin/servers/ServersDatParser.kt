package app.vercors.feature.instance.data.servers

import app.vercors.core.filesystem.readData
import app.vercors.core.nbt.NbtCompound
import app.vercors.core.nbt.getList
import app.vercors.core.nbt.getString
import app.vercors.core.nbt.readNbt
import app.vercors.feature.instance.domain.model.InstalledServer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.io.files.Path

private val logger = KotlinLogging.logger {}

internal suspend fun parseServersDat(path: Path): List<InstalledServer> = try {
    val root = readNbt(path.readData().toByteArray())
    root.getList("servers")?.value.orEmpty().mapNotNull { entry ->
        val compound = entry as? NbtCompound ?: return@mapNotNull null
        val name = compound.getString("name") ?: return@mapNotNull null
        val address = compound.getString("ip") ?: return@mapNotNull null
        InstalledServer(name = name, address = address)
    }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    logger.warn(e) { "Failed to parse servers.dat at $path" }
    emptyList()
}
