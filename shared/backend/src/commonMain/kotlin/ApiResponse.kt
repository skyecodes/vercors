package app.vercors.shared.backend

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.call.*
import io.ktor.client.statement.*

sealed interface ApiResponse<out T, out E> {
    @JvmInline
    value class Success<T>(val data: T) : ApiResponse<T, Nothing>

    @JvmInline
    value class Error<E>(val error: E) : ApiResponse<Nothing, E>
}

context(_: Raise<E>)
fun <T, E> ApiResponse<T, E>.bindResponse(): T = when (this) {
    is ApiResponse.Success -> data
    is ApiResponse.Error -> raise(error)
}

class ApiResponseConverterFactory<E>(
    private val exceptionHandler: suspend (Throwable) -> E
) : Converter.Factory {
    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit
    ): Converter.SuspendResponseConverter<HttpResponse, *>? =
        if (typeData.typeInfo.type == ApiResponse::class)
            object : Converter.SuspendResponseConverter<HttpResponse, ApiResponse<Any, E>> {
                override suspend fun convert(result: KtorfitResult): ApiResponse<Any, E> = when (result) {
                    is KtorfitResult.Success -> ApiResponse.Success(result.response.body(typeData.typeArgs.first().typeInfo))
                    is KtorfitResult.Failure -> ApiResponse.Error(exceptionHandler(result.throwable))
                }
            }
        else null
}
