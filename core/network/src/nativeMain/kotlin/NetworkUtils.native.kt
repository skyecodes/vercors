package app.vercors.core.network

// Kotlin/Native's Curl engine already reports connection/DNS failures as a plain IllegalStateException
// (see isNetworkUnreachable's doc comment), so there's nothing extra to classify here.
actual fun Throwable.isPlatformNetworkUnreachable(): Boolean = false
