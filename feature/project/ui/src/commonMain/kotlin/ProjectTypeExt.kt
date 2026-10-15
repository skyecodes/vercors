package app.vercors.feature.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import app.vercors.feature.project.domain.model.ProjectPlatform
import app.vercors.feature.project.domain.model.ProjectSortBy
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.ui.list.ProjectPlatformView
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.modpack
import com.composables.icons.lucide.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

val ProjectSortBy.displayName: String
    @Composable get() = stringResource(
        when (this) {
            ProjectSortBy.Relevance -> ProjectRes.string.relevance
            ProjectSortBy.Downloads -> ProjectRes.string.downloads
            ProjectSortBy.DateUpdated -> ProjectRes.string.dateUpdated
            ProjectSortBy.DateCreated -> ProjectRes.string.dateCreated
        }
    )

val ProjectType.displayName: String
    @Composable get() = stringResource(
        when (this) {
            ProjectType.Mod -> ProjectRes.string.mod
            ProjectType.Modpack -> SharedRes.string.modpack
            ProjectType.ResourcePack -> ProjectRes.string.resourcePack
            ProjectType.ShaderPack -> ProjectRes.string.shaderPack
            ProjectType.DataPack -> ProjectRes.string.dataPack
            ProjectType.World -> ProjectRes.string.world
        }
    )

val ProjectType.icon: ImageVector
    get() = when (this) {
        ProjectType.Mod -> Lucide.Box
        ProjectType.Modpack -> Lucide.PackageOpen
        ProjectType.ResourcePack -> Lucide.Paintbrush
        ProjectType.ShaderPack -> Lucide.Glasses
        ProjectType.DataPack -> Lucide.Braces
        ProjectType.World -> Lucide.Earth
    }

val ProjectPlatformView?.icon: ImageVector
    @Composable get() = platformIcon(this?.id)

val ProjectPlatform.icon: ImageVector
    @Composable get() = platformIcon(id)

@Composable
private fun platformIcon(platformId: String?): ImageVector = when (platformId) {
    "modrinth" -> vectorResource(ProjectRes.drawable.modrinth)
    "curseforge" -> vectorResource(ProjectRes.drawable.curseforge)
    else -> Lucide.CircleQuestionMark
}
