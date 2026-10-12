package app.vercors.website.data

class DownloadOption(
    val os: Os,
    val arch: Arch,
    val format: DownloadFormat,
    val kind: DownloadKind,
    val url: String,
    val size: Long,
)

enum class Os(val displayName: String) {
    Windows("Windows"),
    MacOs("macOS"),
    Linux("Linux"),
}

enum class Arch(val displayName: String) {
    X64("x64"),
    Arm64("ARM64"),
}

enum class DownloadKind { Gui, Cli }
