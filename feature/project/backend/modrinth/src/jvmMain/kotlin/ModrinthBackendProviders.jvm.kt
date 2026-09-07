package app.vercors.feature.project.backend.modrinth

import de.jensklingenberg.ktorfit.Ktorfit

actual fun Ktorfit.platformCreateModrinthApi(): ModrinthApi = createModrinthApi()
