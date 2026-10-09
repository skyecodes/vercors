package app.vercors.feature.project.api

import app.vercors.feature.project.api.dto.PlatformErrorData
import app.vercors.feature.project.api.dto.PlatformNotFoundData
import app.vercors.feature.project.api.dto.ProjectFileData
import io.ktor.http.*
import opensavvy.spine.api.*
import opensavvy.spine.api.Parameters

object PlatformFiles : StaticResource<Platforms.PlatformId>("files", Platforms.PlatformId) {
    val list by get()
        .parameters(::PlatformFilesParameters)
        .failure<PlatformNotFoundData>(HttpStatusCode.NotFound)
        .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
        .response<List<ProjectFileData>>()

    class PlatformFilesParameters(data: ParameterStorage) : Parameters(data) {
        var fileIds: List<String> by listParameter()
    }
}
