package app.vercors.feature.project.api

import app.vercors.feature.project.api.dto.FeaturedProjectListData
import app.vercors.feature.project.api.dto.PlatformErrorData
import app.vercors.feature.project.api.dto.PlatformNotFoundData
import app.vercors.feature.project.api.dto.ProjectPlatformData
import app.vercors.shared.api.ApiV1
import io.ktor.http.*
import opensavvy.spine.api.DynamicResource
import opensavvy.spine.api.StaticResource

object Platforms : StaticResource<ApiV1>("platforms", ApiV1) {
    val list by get()
        .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
        .response<List<ProjectPlatformData>>()

    object PlatformId : DynamicResource<Platforms>("platformId", Platforms) {
        val get by get()
            .failure<PlatformNotFoundData>(HttpStatusCode.NotFound)
            .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
            .response<ProjectPlatformData>()

        val featuredProjects by get("featured-projects")
            .failure<PlatformNotFoundData>(HttpStatusCode.NotFound)
            .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
            .response<FeaturedProjectListData>()
    }
}
