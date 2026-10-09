package app.vercors.feature.project.api

import app.vercors.feature.project.api.dto.PlatformErrorData
import app.vercors.feature.project.api.dto.PlatformOrProjectNotFoundData
import app.vercors.feature.project.api.dto.ProjectFileData
import io.ktor.http.*
import io.ktor.utils.io.*
import opensavvy.spine.api.*
import opensavvy.spine.api.Parameters

object ProjectFiles : StaticResource<Projects.ProjectId>("files", Projects.ProjectId) {
    val list by get()
        .parameters(::ProjectFilesParameters)
        .failure<PlatformOrProjectNotFoundData>(HttpStatusCode.NotFound)
        .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
        .response<List<ProjectFileData>>()

    class ProjectFilesParameters(data: ParameterStorage) : Parameters(data) {
        var gameVersion: String? by parameter()
        var loader: String? by parameter()
    }

    object FileId : DynamicResource<ProjectFiles>("fileId", ProjectFiles) {
        val get by get()
            .failure<PlatformOrProjectNotFoundData>(HttpStatusCode.NotFound)
            .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
            .response<ProjectFileData>()

        val download by get("download")
            .failure<PlatformOrProjectNotFoundData>(HttpStatusCode.NotFound)
            .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
            .response<ByteReadChannel>()
    }
}
