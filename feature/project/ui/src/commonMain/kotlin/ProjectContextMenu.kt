package app.vercors.feature.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import app.vercors.core.ui.component.AppContextMenuItem
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.primary
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.ui.details.isInstallable
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.openDetails
import app.vercors.shared.ui.openInBrowser
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.ExternalLink
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun projectContextMenuItems(
    project: BriefProjectView,
    onInstallClick: (BriefProjectView) -> Unit,
    onProjectClick: (ProjectId) -> Unit,
): () -> List<AppContextMenuItem> {
    val uriHandler = LocalUriHandler.current
    val installLabel = stringResource(ProjectRes.string.install)
    val openDetailsLabel = stringResource(SharedRes.string.openDetails)
    val openInBrowserLabel = stringResource(SharedRes.string.openInBrowser)
    val primaryColor = Theme[colors][primary]

    return {
        listOfNotNull(
            if (project.type.isInstallable) {
                AppContextMenuItem(
                    label = installLabel,
                    imageVector = Lucide.Download,
                    contentColor = primaryColor,
                    onClick = { onInstallClick(project) },
                )
            } else null,
            AppContextMenuItem(
                label = openDetailsLabel,
                imageVector = Lucide.Info,
                onClick = { onProjectClick(project.id) },
            ),
            AppContextMenuItem(
                label = openInBrowserLabel,
                imageVector = Lucide.ExternalLink,
                onClick = { uriHandler.openUri(project.url) },
            ),
        )
    }
}
