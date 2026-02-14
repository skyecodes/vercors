package app.vercors.shared.backend

actual fun platformEnv(name: String): String? = System.getenv(name)
