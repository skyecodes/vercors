package app.vercors.launcher.common.domain.system

interface SystemService {
    val osType: OsType
    val osVersion: String
    val osArch: OsArch
}

enum class OsType(val id: String) {
    Windows("windows"), Linux("linux"), MacOs("osx")
}

enum class OsArch(val id: String) {
    X86("x86"), X64("x64")
}
