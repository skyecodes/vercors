package app.vercors.feature.project.domain.model.pack

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.instance.domain.model.localimport.PrismInstanceInfo
import app.vercors.feature.project.domain.model.modpack.ModpackManifest

sealed interface LocalPack {
    val name: String
    val gameVersion: String
    val loaderType: ModLoaderType?
    val loaderVersion: String?
    val sourcePath: String
    val modpackInfo: InstanceModpackInfo?
    val versionName: String? get() = modpackInfo?.versionName

    data class Vercors(override val sourcePath: String, val info: VercorsPackInfo) : LocalPack {
        override val name: String get() = info.name
        override val gameVersion: String get() = info.gameVersion
        override val loaderType: ModLoaderType? get() = info.modLoader?.type
        override val loaderVersion: String? get() = info.modLoader?.version
        override val modpackInfo: InstanceModpackInfo? get() = info.modpackInfo
    }

    data class Prism(override val sourcePath: String, val info: PrismInstanceInfo) : LocalPack {
        override val name: String get() = info.name
        override val gameVersion: String get() = info.gameVersion
        override val loaderType: ModLoaderType? get() = info.loaderType
        override val loaderVersion: String? get() = info.loaderVersion
        override val modpackInfo: InstanceModpackInfo? get() = info.modpackInfo
    }

    data class Mrpack(override val sourcePath: String, val manifest: ModpackManifest) : LocalPack {
        override val name: String get() = manifest.name
        override val gameVersion: String get() = manifest.gameVersion
        override val loaderType: ModLoaderType? get() = manifest.loaderType
        override val loaderVersion: String? get() = manifest.loaderVersion

        // A local .mrpack carries no project/version ID for the pack itself
        override val modpackInfo: InstanceModpackInfo? get() = null
    }
}
