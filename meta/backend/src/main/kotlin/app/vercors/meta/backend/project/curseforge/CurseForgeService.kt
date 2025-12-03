package app.vercors.meta.backend.project.curseforge

import app.vercors.meta.backend.project.PlatformServiceBase
import app.vercors.meta.lib.project.PlatformData
import app.vercors.meta.lib.project.ProjectTypeData
import org.koin.core.annotation.Single

internal const val MINECRAFT_GAME_ID = 432
internal const val MOD_CLASS_ID = 6
internal const val MODPACK_CLASS_ID = 4471
internal const val RESOURCEPACK_CLASS_ID = 12
internal const val SHADERPACK_CLASS_ID = 6552

@Single
class CurseForgeService(
    private val api: CurseForgeApi
) : PlatformServiceBase {
    override val info: PlatformData = PlatformData(
        id = "curseforge",
        name = "CurseForge",
        baseUrl = "https://www.curseforge.com/minecraft"
    )

    override suspend fun search(
        type: ProjectTypeData,
        query: String?,
        limit: Int?
    ) = api.search(
        gameId = MINECRAFT_GAME_ID,
        classId = type.toClassId(),
        searchFilter = query,
        pageSize = limit
    ).convert()
}


