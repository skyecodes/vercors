package app.vercors.feature.instance.ui.create

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.instance.ui.*
import com.composables.icons.lucide.Import
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.PackagePlus
import com.composables.icons.lucide.SquarePlus
import opensavvy.state.progressive.ProgressiveOutcome
import org.jetbrains.compose.resources.StringResource

@Immutable
data class CreateInstanceState(
    val selectedTab: CreateInstanceTab,
    val instanceName: String,
    val selectedGameVersion: GameVersionView?,
    val gameVersionList: ProgressiveOutcome<GameVersionError.InvalidManifest, List<GameVersionView>>,
    val showSnapshot: Boolean,
    val gameVersionFilter: String,
    val validationErrors: List<InstanceValidationError>,
) {
    val canCreateInstance: Boolean = instanceName.length >= 3 && selectedGameVersion != null
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
