package app.vercors.feature.project.api

import app.vercors.feature.project.api.dto.*
import io.ktor.http.*
import opensavvy.spine.api.*
import opensavvy.spine.api.Parameters

object PlatformFiles : StaticResource<Platforms.PlatformId>("files", Platforms.PlatformId) {
    val list by get()
        .parameters(::PlatformFilesParameters)
        .failure<PlatformNotFoundData>(HttpStatusCode.NotFound)
        .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
        .response<List<ProjectFileData>>()

    val lookup by post("lookup")
        .request<List<FileHashesData>>()
        .failure<PlatformNotFoundData>(HttpStatusCode.NotFound)
        .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
        .response<List<ProjectFileMatchData>>()

    class PlatformFilesParameters(data: ParameterStorage) : Parameters(data) {
        var fileIds: List<String> by listParameter()
    }
}
