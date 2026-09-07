package app.vercors.feature.project.backend

import app.vercors.feature.project.api.dto.PlatformErrorData
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.call.*
import io.ktor.client.statement.*

sealed interface PlatformResponse<out T> {
    @JvmInline
    value class Success<T>(val data: T) : PlatformResponse<T>

    @JvmInline
    value class Error(val error: PlatformErrorData) : PlatformResponse<Nothing>
}

context(_: Raise<PlatformErrorData>)
fun <T> PlatformResponse<T>.bindResponse(): T = when (this) {
    is PlatformResponse.Success -> data
    is PlatformResponse.Error -> raise(error)
}

class PlatformResponseConverterFactory(
    private val exceptionHandler: suspend (Throwable) -> PlatformErrorData = defaultExceptionHandler
) : Converter.Factory {
    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit
    ): Converter.SuspendResponseConverter<HttpResponse, *>? =
        if (typeData.typeInfo.type == PlatformResponse::class)
            object : Converter.SuspendResponseConverter<HttpResponse, PlatformResponse<Any>> {
                override suspend fun convert(result: KtorfitResult): PlatformResponse<Any> = when (result) {
                    is KtorfitResult.Success -> PlatformResponse.Success(result.response.body(typeData.typeArgs.first().typeInfo))
                    is KtorfitResult.Failure -> PlatformResponse.Error(exceptionHandler(result.throwable))
                }
            }
        else null
}

val defaultExceptionHandler: suspend (Throwable) -> PlatformErrorData =
    { PlatformErrorData(it::class.simpleName ?: "unknown", it.message) }
