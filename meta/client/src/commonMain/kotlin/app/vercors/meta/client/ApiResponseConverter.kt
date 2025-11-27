package app.vercors.meta.client

import app.vercors.meta.lib.ApiError
import app.vercors.meta.lib.ApiResponse
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

class ApiResponseConverter : Converter.Factory {
    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit
    ): Converter.SuspendResponseConverter<HttpResponse, *>? =
        if (typeData.typeInfo.type == ApiResponse::class)
            object : Converter.SuspendResponseConverter<HttpResponse, ApiResponse<Any>> {
                override suspend fun convert(result: KtorfitResult): ApiResponse<Any> = when (result) {
                    is KtorfitResult.Success -> ApiResponse.Success(result.response.body(typeData.typeArgs.first().typeInfo))
                    is KtorfitResult.Failure -> when (val e = result.throwable) {
                        is IOException -> ApiResponse.Error.NetworkError(e)
                        is ClientRequestException -> ApiResponse.Error.Http.ClientError(
                            e.response.status.value,
                            e.errorBody(),
                            e
                        )

                        is ServerResponseException -> ApiResponse.Error.Http.ServerError(
                            e.response.status.value,
                            e.errorBody(),
                            e
                        )

                        is SerializationException -> ApiResponse.Error.SerializationError(e)
                        else -> ApiResponse.Error.UnknownError(e)
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