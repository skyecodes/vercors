package app.vercors.feature.launcher.data.service

import app.vercors.shared.data.FileValidationRequest
import kotlinx.io.files.Path

sealed class DependencyFile(
    url: String,
    path: Path,
    size: Long?,
    sha1: String?
) : FileValidationRequest.Url(url, path, size, sha1) {
    class Basic(
        url: String,
        path: Path,
        size: Long?,
        sha1: String?
    ) : DependencyFile(url, path, size, sha1)

    class Native(
        url: String,
        path: Path,
        size: Long?,
        sha1: String?,
        val extractDest: Path,
        val extractExclude: List<String>?
    ) : DependencyFile(url, path, size, sha1)
}

class InstanceDependencyList(
    val client: DependencyFile,
    val libraries: List<DependencyFile>,
    val natives: List<DependencyFile.Native>,
    val assets: List<DependencyFile>,
    val logging: DependencyFile?
) {
    val allDependencies: List<DependencyFile> = buildList {
        add(client)
        addAll(libraries)
        addAll(natives)
        addAll(assets)
        logging?.let { add(it) }
    }
}
