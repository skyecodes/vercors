package app.vercors.meta.backend.project.modrinth

import app.vercors.meta.backend.project.modrinth.dto.ModrinthCategory
import app.vercors.meta.backend.project.modrinth.dto.ModrinthProject
import app.vercors.meta.backend.project.modrinth.dto.ModrinthProjectMember
import app.vercors.meta.backend.project.modrinth.dto.ModrinthProjectSearchResult
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

interface ModrinthApi {
    @GET("v2/tag/project_type")
    suspend fun getProjectTypes(): ModrinthApiResponse<List<String>>

    @GET("v2/tag/category")
    suspend fun getCategories(): ModrinthApiResponse<List<ModrinthCategory>>

    @GET("v2/search")
    suspend fun search(
        @Query("query") query: String? = null,
        @Query("facets") facets: String? = null,
        @Query("index") index: String? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): ModrinthApiResponse<ModrinthProjectSearchResult>

    @GET("v2/project/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: String): ModrinthApiResponse<ModrinthProject>

    @GET("v2/project/{projectId}/members")
    suspend fun getProjectMembers(@Path("projectId") projectId: String): ModrinthApiResponse<List<ModrinthProjectMember>>
}

@Single
fun provideModrinthApi(
    httpClient: HttpClient
): ModrinthApi = Ktorfit.Builder()
    .httpClient(httpClient)
    .converterFactories(ModrinthApiResponseConverterFactory())
    .baseUrl("https://api.modrinth.com/")
    .build()
    .createModrinthApi()

sealed interface ModrinthApiResponse<out T> {
    @JvmInline
    value class Success<T>(val data: T) : ModrinthApiResponse<T>
    data class Error(val error: ModrinthError) : ModrinthApiResponse<Nothing>
}

fun <T> ModrinthApiResponse<T>.getOrThrow(): T = when (this) {
    is ModrinthApiResponse.Error -> throw ModrinthApiException(error)
    is ModrinthApiResponse.Success -> data
}

@Serializable
data class ModrinthError(
    val error: String,
    val description: String?
)

class ModrinthApiException(error: ModrinthError) : Exception(error.description ?: error.error)

private class ModrinthApiResponseConverterFactory : Converter.Factory {
    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit
    ): Converter.SuspendResponseConverter<HttpResponse, *>? =
        if (typeData.typeInfo.type == ModrinthApiResponse::class)
            object : Converter.SuspendResponseConverter<HttpResponse, ModrinthApiResponse<Any>> {
                override suspend fun convert(result: KtorfitResult): ModrinthApiResponse<Any> = when (result) {
                    is KtorfitResult.Success -> ModrinthApiResponse.Success(result.response.body(typeData.typeArgs.first().typeInfo))
                    is KtorfitResult.Failure -> {
                        when (val e = result.throwable) {
                            is ResponseException -> ModrinthApiResponse.Error(e.response.body())
                            else -> ModrinthApiResponse.Error(
                                ModrinthError(
                                    e::class.simpleName ?: "unknown",
                                    e.message
                                )
                            )
                        }
                    }
                }
            }
        else null
}
