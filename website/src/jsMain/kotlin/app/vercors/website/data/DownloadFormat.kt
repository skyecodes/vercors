package app.vercors.website.data

enum class DownloadFormat(val label: String, val description: String) {
    Exe("EXE", "Installer"),
    Nsis("NSIS", "Installer"),
    Msi("MSI", "Windows Installer package"),
    AppX("AppX", "Windows app package"),
    Portable("Portable", "Single executable, no install"),
    Dmg("DMG", "Disk image"),
    Pkg("PKG", "Installer package"),
    AppImage("AppImage", "Runs on most distributions"),
    Deb("DEB", "Debian, Ubuntu and derivatives"),
    Rpm("RPM", "Fedora, openSUSE and derivatives"),
    Pacman("Pacman", "Arch Linux and derivatives"),
    Flatpak("Flatpak", "Sandboxed package"),
    Snap("Snap", "Sandboxed package"),
    Zip("ZIP", "Archive"),
    TarGz("TAR.GZ", "Archive"),
}
