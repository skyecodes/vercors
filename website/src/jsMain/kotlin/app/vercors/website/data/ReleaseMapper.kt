package app.vercors.website.data

import app.vercors.website.data.dto.GitHubAsset
import app.vercors.website.data.dto.GitHubRelease

fun GitHubRelease.toRelease(): Release {
    val version = tagName.removePrefix("v")
    return Release(
        version = version,
        url = htmlUrl,
        publishedAt = publishedAt,
        isPrerelease = prerelease,
        downloads = assets.mapNotNull { it.toDownloadOption(version) },
    )
}

// Asset names come from Nucleus packaging (GUI) and release.yml (CLI), e.g. vercors-1.0.0-linux-x86_64.rpm,
// vercors-1.0.0-win-x64-nsis.exe, vercors-cli-1.0.0-macos-arm64.tar.gz
private fun GitHubAsset.toDownloadOption(version: String): DownloadOption? {
    val (kind, rest) = when {
        name.startsWith("vercors-cli-$version-") -> DownloadKind.Cli to name.removePrefix("vercors-cli-$version-")
        name.startsWith("vercors-$version-") -> DownloadKind.Gui to name.removePrefix("vercors-$version-")
        else -> return null
    }
    val parts = rest.split('-', limit = 3)
    if (parts.size < 2) return null
    val os = parts[0].toOs() ?: return null
    val archAndExtension = parts.drop(1).joinToString("-")
    val arch = archAndExtension.substringBefore('.').substringBefore('-').toArch() ?: return null
    val variant = archAndExtension.substringAfter('-', "")
    val format = when {
        variant.isNotEmpty() -> variant.toVariantFormat()
        else -> archAndExtension.substringAfter('.').toExtensionFormat()
    } ?: return null
    return DownloadOption(os, arch, format, kind, browserDownloadUrl, size)
}

private fun String.toOs(): Os? = when (this) {
    "win", "windows" -> Os.Windows
    "mac", "macos" -> Os.MacOs
    "linux" -> Os.Linux
    else -> null
}

private fun String.toArch(): Arch? = when (this) {
    "x64", "amd64", "x86_64" -> Arch.X64
    "arm64", "aarch64" -> Arch.Arm64
    else -> null
}

private fun String.toVariantFormat(): DownloadFormat? = when (this) {
    "nsis.exe" -> DownloadFormat.Nsis
    "portable.exe" -> DownloadFormat.Portable
    else -> null
}

private fun String.toExtensionFormat(): DownloadFormat? = when (this) {
    "exe" -> DownloadFormat.Exe
    "msi" -> DownloadFormat.Msi
    "appx" -> DownloadFormat.AppX
    "dmg" -> DownloadFormat.Dmg
    "pkg" -> DownloadFormat.Pkg
    "AppImage" -> DownloadFormat.AppImage
    "deb" -> DownloadFormat.Deb
    "rpm" -> DownloadFormat.Rpm
    "pacman" -> DownloadFormat.Pacman
    "flatpak" -> DownloadFormat.Flatpak
    "snap" -> DownloadFormat.Snap
    "zip" -> DownloadFormat.Zip
    "tar.gz" -> DownloadFormat.TarGz
    else -> null
}
