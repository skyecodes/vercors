package app.vercors.core.network

import java.nio.channels.UnresolvedAddressException

actual fun Throwable.isPlatformNetworkUnreachable(): Boolean = this is UnresolvedAddressException
