package app.vercors.feature.project.domain.model.pack

import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions

data class VercorsPackInfo(
    val name: String,
    val gameVersion: String,
    val modLoader: InstanceModLoader?,
    val modpackInfo: InstanceModpackInfo?,
    val jvmMemoryOptions: JvmMemoryOptions,
    val nativeLibraryOptions: NativeLibraryOptions,
    val iconExtension: String?,
    val manifest: ModpackManifest
)
