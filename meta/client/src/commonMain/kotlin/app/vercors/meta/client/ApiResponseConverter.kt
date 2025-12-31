package app.vercors.meta.client

import app.vercors.meta.lib.ApiError
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.serialization.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

internal class ApiResponseConverter : Converter.Factory {
    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit
    ): Converter.SuspendResponseConverter<HttpResponse, *>? =
        if (typeData.typeInfo.type == ApiResponse::class)
            object : Converter.SuspendResponseConverter<HttpResponse, ApiResponse<Any>> {
                override suspend fun convert(result: KtorfitResult): ApiResponse<Any> = when (result) {
                    is KtorfitResult.Success -> try {
                        ApiResponse.Success(result.response.body(typeData.typeArgs.first().typeInfo))
                    } catch (e: Exception) {
                        currentCoroutineContext().ensureActive()
                        ApiResponse.Failure.UnknownError(e)
                    }
                    is KtorfitResult.Failure -> when (val e = result.throwable) {
                        is IOException -> ApiResponse.Failure.NetworkError(e)
                        is ClientRequestException -> ApiResponse.Failure.Http.ClientError(
                            e.response.status.value,
                            e.errorBody(),
                            e
                        )

                        is ServerResponseException -> ApiResponse.Failure.Http.ServerError(
                            e.response.status.value,
                            e.errorBody(),
                            e
                        )

                        is SerializationException, is ContentConvertException -> ApiResponse.Failure.SerializationError(
                            e
                        )

                        is Exception -> ApiResponse.Failure.UnknownError(e)
                        else -> throw e
                    }
                }
            }
        else null

    private suspend fun ResponseException.errorBody(): ApiError? = try {
        response.body()
    } catch (_: SerializationException) {
        null
    }
}
