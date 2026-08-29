package app.vercors.feature.project.api

import app.vercors.feature.project.api.dto.PlatformErrorData
import app.vercors.feature.project.api.dto.PlatformNotFoundData
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

    object ProjectFileId : DynamicResource<ProjectFiles>("fileId", ProjectFiles) {
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

/** Resolves many files by ID in one request, potentially across different projects — used to batch-resolve a
 *  modpack manifest's file list instead of one request per file. */
object PlatformFiles : StaticResource<Platforms.PlatformId>("files", Platforms.PlatformId) {
    val get by get()
        .parameters(::PlatformFilesParameters)
        .failure<PlatformNotFoundData>(HttpStatusCode.NotFound)
        .failure<PlatformErrorData>(HttpStatusCode.InternalServerError)
        .response<List<ProjectFileData>>()

    class PlatformFilesParameters(data: ParameterStorage) : Parameters(data) {
        var fileIds: List<String> by listParameter()
    }
}
