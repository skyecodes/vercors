package app.vercors.shared.backend

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import kotlinx.serialization.json.Json

context(_: Raise<E>)
inline fun <reified T, E> Json.decodeEnumParameter(value: String, onUnknown: (String) -> E): T = try {
    decodeFromString<T>("\"$value\"")
} catch (_: IllegalArgumentException) {
    raise(onUnknown(value))
}
