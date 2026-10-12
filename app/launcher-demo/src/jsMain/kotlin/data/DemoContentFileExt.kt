package app.vercors.launcher.demo.data

import app.vercors.feature.instance.ui.details.content.InstalledContentFileView

fun DemoContentFile.toView(): InstalledContentFileView = InstalledContentFileView(
    fileName = fileName,
    title = title,
    description = description,
    version = version,
    iconFilePath = icon?.let { "/icons/$it" },
    enabled = true,
    providers = emptyList(),
    lastModified = null,
)
