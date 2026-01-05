package app.vercors.feature.instance.ui.create

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import app.vercors.core.domain.Resource
import app.vercors.core.domain.orNull
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.instance.ui.*
import com.composables.icons.lucide.Import
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.PackagePlus
import com.composables.icons.lucide.SquarePlus
import org.jetbrains.compose.resources.StringResource

@Immutable
data class CreateInstanceState(
    val selectedTab: CreateInstanceTab = CreateInstanceTab.Custom,
    val instanceName: String = "",
    val selectedGameVersion: GameVersionView? = null,
    val gameVersionList: Resource<List<GameVersionView>> = Resource.Unloaded,
    val showSnapshot: Boolean = false,
    val gameVersionFilter: String = "",
) {
    val filteredGameVersionList: List<GameVersionView> = gameVersionList.orNull()?.filter {
        (gameVersionFilter.isBlank() || it.id.contains(
            gameVersionFilter,
            ignoreCase = true
        )) && (showSnapshot || !it.isSnapshot)
    } ?: emptyList()
    val canCreateInstance: Boolean = instanceName.isNotBlank() && selectedGameVersion != null
    val filteredSelectedGameVersion: GameVersionView? = filteredGameVersionList.find { it == selectedGameVersion }
        ?: filteredGameVersionList.firstOrNull { it.isLatestRelease }
        ?: filteredGameVersionList.firstOrNull()
}

enum class CreateInstanceTab(
    val icon: ImageVector,
    val title: StringResource,
    val description: StringResource,
) {
    Custom(
        icon = Lucide.SquarePlus,
        title = InstanceRes.string.createCustomInstance,
        description = InstanceRes.string.createCustomInstanceDescription,
    ),
    Modpack(
        icon = Lucide.PackagePlus,
        title = InstanceRes.string.importModpack,
        description = InstanceRes.string.importModpackDescription,
    ),
    Import(
        icon = Lucide.Import,
        title = InstanceRes.string.importOtherLauncher,
        description = InstanceRes.string.importOtherLauncherDescription,
    )
}
