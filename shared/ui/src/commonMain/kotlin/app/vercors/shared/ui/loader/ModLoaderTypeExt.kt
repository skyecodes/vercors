package app.vercors.shared.ui.loader

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import app.vercors.shared.domain.loader.ModLoaderType
import app.vercors.shared.ui.res.*
import org.jetbrains.compose.resources.vectorResource

val ModLoaderType.icon: ImageVector
    @Composable get() = vectorResource(
        when (this) {
            ModLoaderType.Forge -> Res.drawable.forge
            ModLoaderType.Fabric -> Res.drawable.fabric
            ModLoaderType.Quilt -> Res.drawable.quilt
            ModLoaderType.NeoForge -> Res.drawable.neoforge
        }
    )
