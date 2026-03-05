package app.vercors.feature.project.api

import app.vercors.feature.project.api.dto.*
import io.ktor.http.*
import opensavvy.spine.api.*
import opensavvy.spine.api.Parameters

object Projects : StaticResource<Platforms.PlatformId>("projects", Platforms.PlatformId) {
    val search by get()
        .parameters(::ProjectSearchParameters)
        .failure<UnsupportedProjectTypeData>(HttpStatusCode.BadRequest)
        .failure<PlatformNotFoundData>(HttpStatusCode.NotFound)
        .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
        .response<ProjectSearchResponseData>()

    class ProjectSearchParameters(data: ParameterStorage) : Parameters(data) {
        var type: String? by parameter()
        var loaders: List<String> by listParameter()
        var gameVersions: List<String> by listParameter()
        var categories: List<String> by listParameter()
        var query: String? by parameter()
        var sortBy: String? by parameter()
        var pageSize: Int? by parameter()
        var offset: Int? by parameter()
    }

    object ProjectId : DynamicResource<Projects>("projectId", Projects) {
        val get by get()
            .failure<PlatformOrProjectNotFoundData>(HttpStatusCode.NotFound)
            .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
            .response<ProjectData>()
    }
}
