package app.vercors.feature.project.backend

import app.vercors.feature.project.backend.curseforge.CurseForgeApi
import app.vercors.feature.project.backend.curseforge.createCurseForgeApi
import app.vercors.feature.project.backend.modrinth.ModrinthApi
import app.vercors.feature.project.backend.modrinth.createModrinthApi
import de.jensklingenberg.ktorfit.Ktorfit

actual fun Ktorfit.platformCreateModrinthApi(): ModrinthApi = createModrinthApi()

actual fun Ktorfit.platformCreateCurseForgeApi(): CurseForgeApi = createCurseForgeApi()
