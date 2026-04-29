package app.vercors.feature.config.domain.model

data class Configuration(
    val metaApiUrl: String,
    val parallelDownloads: Int,
    val parallelFilesystemOperations: Int,
    val menuDrawerOpen: Boolean,
    val animations: Boolean,
    val preferredPlatform: String,
    val homeCategories: List<HomeCategoryConfig>,
    val selectedThemeId: String,
) {
    companion object {
        const val DEFAULT_THEME_ID = "catppuccin_mocha"

        val Default = Configuration(
            metaApiUrl = "https://meta.vercors.app/",
            parallelDownloads = 10,
            parallelFilesystemOperations = 10,
            menuDrawerOpen = true,
            animations = true,
            preferredPlatform = "modrinth",
            homeCategories = HomeCategoryConfig.entries,
            selectedThemeId = DEFAULT_THEME_ID,
        )
    }
}
