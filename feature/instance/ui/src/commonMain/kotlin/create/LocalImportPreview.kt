package app.vercors.feature.instance.ui.create

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.instance.domain.model.localimport.PrismInstanceInfo
import app.vercors.feature.project.domain.model.modpack.ModpackManifest

@Immutable
sealed interface LocalImportPreview {
    val name: String
    val gameVersion: String
    val loaderType: ModLoaderType?
    val loaderVersion: String?
    val sourcePath: String
    val modpackInfo: InstanceModpackInfo?

    data class Prism(
        override val name: String,
        override val gameVersion: String,
        override val loaderType: ModLoaderType?,
        override val loaderVersion: String?,
        override val sourcePath: String,
        val info: PrismInstanceInfo,
    ) : LocalImportPreview {
        override val modpackInfo: InstanceModpackInfo? get() = info.modpackInfo
    }

    data class Mrpack(
        override val name: String,
        override val gameVersion: String,
        override val loaderType: ModLoaderType?,
        override val loaderVersion: String?,
        override val sourcePath: String,
        val manifest: ModpackManifest,
    ) : LocalImportPreview {
        // A local .mrpack carries no project/version ID for the pack itself
        override val modpackInfo: InstanceModpackInfo? get() = null
    }
}
