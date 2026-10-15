package app.vercors.core.system

internal fun String.toOsArch(): OsArch = when (lowercase()) {
    "aarch64", "arm64" -> OsArch.Arm64
    "amd64", "x86_64", "x64" -> OsArch.X64
    else -> OsArch.X86
}
