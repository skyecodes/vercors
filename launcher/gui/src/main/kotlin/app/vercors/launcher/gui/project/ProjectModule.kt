package app.vercors.launcher.gui.project

import app.vercors.launcher.gui.navigation.appNavigation
import app.vercors.launcher.gui.project.details.ProjectDetailsRoute
import app.vercors.launcher.gui.project.list.ProjectListRoute
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.subtitle
import app.vercors.shared.ui.textStyles
import com.composeunstyled.theme.Theme
import org.koin.dsl.module

val ProjectModule = module {
    appNavigation<ProjectListRoute> { _, _ ->
        AppText("Project list", style = Theme[textStyles][subtitle])
    }
    appNavigation<ProjectDetailsRoute> { _, _ ->
        AppText("Project details")
    }
}
