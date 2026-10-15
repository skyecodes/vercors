package app.vercors.feature.project.data.dto

import app.vercors.shared.data.dto.JvmMemoryOptionsData
import app.vercors.shared.data.dto.NativeLibraryOptionsData
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
internal data class VercorsPackManifest(
    @EncodeDefault val formatVersion: Int = 1,
    val name: String,
    val gameVersion: String,
    val modLoader: VercorsPackModLoader? = null,
    val modpackInfo: VercorsPackModpackInfo? = null,
    val jvmMemoryOptions: JvmMemoryOptionsData = JvmMemoryOptionsData(),
    val nativeLibraryOptions: NativeLibraryOptionsData = NativeLibraryOptionsData(),
    val iconExtension: String? = null,
    @EncodeDefault val files: List<VercorsPackFile> = emptyList()
)

@Serializable
internal data class VercorsPackModLoader(val type: String, val version: String)

@Serializable
internal data class VercorsPackModpackInfo(
    val platformId: String,
    val projectId: String,
    val fileId: String,
    val versionName: String? = null
)

@Serializable
internal data class VercorsPackFile(
    val path: String,
    val platformId: String,
    val projectId: String,
    val fileId: String,
    val sha1: String,
    val size: Long
)
