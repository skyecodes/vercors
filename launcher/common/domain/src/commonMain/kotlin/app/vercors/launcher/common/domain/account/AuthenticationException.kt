package app.vercors.launcher.common.domain.account

open class AuthenticationException(
    message: String? = "An authentication error has occurred",
    cause: Exception? = null
) : Exception(message, cause)