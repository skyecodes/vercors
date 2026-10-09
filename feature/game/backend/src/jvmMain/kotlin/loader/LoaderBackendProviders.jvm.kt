package app.vercors.feature.game.backend.loader

import de.jensklingenberg.ktorfit.Ktorfit

actual fun Ktorfit.platformCreateMetaLoaderApi(): MetaLoaderApi = createMetaLoaderApi()

actual fun Ktorfit.platformCreateMavenApi(): MavenApi = createMavenApi()
