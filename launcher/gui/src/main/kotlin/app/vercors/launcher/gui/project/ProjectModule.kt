package app.vercors.launcher.gui.project

import app.vercors.launcher.gui.project.details.ProjectDetailsRoute
import app.vercors.launcher.gui.project.list.ProjectListRoute
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.subtitle
import app.vercors.shared.ui.textStyles
import com.composeunstyled.theme.Theme
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

val ProjectModule = module {
    navigation<ProjectListRoute> {
        AppText("Project list", style = Theme[textStyles][subtitle])
    }
    navigation<ProjectDetailsRoute> {
        AppText("Project details")
    }
}
