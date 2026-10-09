package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.shared.backend.ApiResponse

typealias LoaderResponse<T> = ApiResponse<T, LoaderErrorData>

val loaderExceptionHandler: suspend (Throwable) -> LoaderErrorData =
    { LoaderErrorData(it::class.simpleName ?: "unknown", it.message) }
