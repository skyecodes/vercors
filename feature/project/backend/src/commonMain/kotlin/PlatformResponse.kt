package app.vercors.feature.project.backend

import app.vercors.feature.project.api.dto.PlatformErrorData
import app.vercors.shared.backend.ApiResponse

typealias PlatformResponse<T> = ApiResponse<T, PlatformErrorData>

val defaultExceptionHandler: suspend (Throwable) -> PlatformErrorData =
    { PlatformErrorData(it::class.simpleName ?: "unknown", it.message) }
