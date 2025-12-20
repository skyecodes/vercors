package app.vercors.meta.backend.project.curseforge

import app.vercors.meta.backend.project.PlatformInfo
import app.vercors.meta.backend.project.PlatformServiceBase
import app.vercors.meta.lib.project.*
import org.koin.core.annotation.Single

internal const val MINECRAFT_GAME_ID = 432
internal const val MOD_CLASS_ID = 6
internal const val MODPACK_CLASS_ID = 4471
internal const val RESOURCEPACK_CLASS_ID = 12
internal const val SHADERPACK_CLASS_ID = 6552
internal const val DATAPACK_CLASS_ID = 6945
internal const val WORLD_CLASS_ID = 17

internal const val CURSEFORGE_PLATFORM_ID = "curseforge"

@Single
class CurseForgeService(
    private val api: CurseForgeApi
) : PlatformServiceBase {
    override val info = PlatformInfo(
        id = CURSEFORGE_PLATFORM_ID,
        name = "CurseForge",
        baseUrl = "https://www.curseforge.com/minecraft",
    )

    override suspend fun getCategories(): Map<ProjectTypeData, List<ProjectCategoryData>> = api.getCategories(
        gameId = MINECRAFT_GAME_ID
    ).data.let { categories ->
        categories.filter { it.isClass == true && it.convertToType() != null }.associate { type ->
            type.convertToType()!! to categories.filter { it.classId == type.id && it.parentCategoryId == type.id }
                .map {
                    ProjectCategoryData(
                        id = "${it.id}",
                        slug = it.slug,
                        name = it.name
                    )
                }
        }
    }

    override suspend fun search(
        projectType: ProjectTypeData?,
        loaders: List<ProjectLoaderData>?,
        gameVersions: List<String>?,
        categories: List<String>?,
        query: String?,
        firstIndex: Int?,
        pageSize: Int?
    ): ProjectSearchResponseData = api.search(
        gameId = MINECRAFT_GAME_ID,
        classId = projectType?.toCurseForgeClassId(),
        categoryIds = categories?.joinToString(",", "[", "]") { "\"$it\"" },
        gameVersions = gameVersions?.joinToString(",", "[", "]") { "\"$it\"" },
        modLoaderTypes = loaders?.joinToString(",", "[", "]") { "\"${it.toCurseForgeModLoaderType()}\"" },
        searchFilter = query,
        index = firstIndex,
        pageSize = pageSize
    ).convert()

    override suspend fun getProject(projectId: String): ProjectData = api.getProject(projectId).data.convert()
}
