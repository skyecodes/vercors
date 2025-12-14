package app.vercors.shared.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

typealias ResourceFlow<T> = Flow<Resource<T>>

fun <T> resourceFlow(provider: suspend () -> Result<T>): ResourceFlow<T> = flow {
    emit(Resource.Loading)
    emit(Resource.Loaded(provider()))
}
