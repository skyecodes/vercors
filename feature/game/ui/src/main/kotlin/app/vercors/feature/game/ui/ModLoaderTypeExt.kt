package app.vercors.feature.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import app.vercors.feature.game.domain.model.ModLoaderType
import org.jetbrains.compose.resources.vectorResource

val ModLoaderType.icon: ImageVector
    @Composable get() = vectorResource(
        when (this) {
            ModLoaderType.Forge -> GameRes.drawable.forge
            ModLoaderType.Fabric -> GameRes.drawable.fabric
            ModLoaderType.Quilt -> GameRes.drawable.quilt
            ModLoaderType.NeoForge -> GameRes.drawable.neoforge
        }
    )
