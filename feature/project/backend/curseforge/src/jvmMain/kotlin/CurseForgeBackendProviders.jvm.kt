package app.vercors.feature.project.backend.curseforge

import de.jensklingenberg.ktorfit.Ktorfit

actual fun Ktorfit.platformCreateCurseForgeApi(): CurseForgeApi = createCurseForgeApi()
