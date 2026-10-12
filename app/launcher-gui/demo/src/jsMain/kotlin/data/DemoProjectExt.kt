package app.vercors.launcher.demo.data

import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.ui.BriefProjectView
import app.vercors.launcher.demo.demoIconUrl

private const val MODRINTH_PLATFORM_ID = "modrinth"

fun DemoProject.toView(): BriefProjectView = BriefProjectView(
    id = ProjectId(id, MODRINTH_PLATFORM_ID),
    name = title,
    type = type,
    loaders = loaders,
    description = description,
    author = author,
    url = "https://modrinth.com/project/$slug",
    downloads = downloads,
    lastUpdated = dateModified.shiftedToNow(),
    logoUrl = icon?.let(::demoIconUrl),
    imageUrl = banner?.let(::demoIconUrl),
)
