package app.vercors.launcher.home.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.vercors.launcher.core.generated.resources.play
import app.vercors.launcher.core.generated.resources.vercors
import app.vercors.launcher.core.presentation.CoreDrawable
import app.vercors.launcher.home.presentation.model.HomeInstanceStatusUi
import app.vercors.launcher.home.presentation.model.HomeSectionItemUi
import app.vercors.launcher.instance.generated.resources.last_played
import app.vercors.launcher.instance.generated.resources.running
import app.vercors.launcher.instance.presentation.InstanceString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun HomeInstanceCard(
    instance: HomeSectionItemUi.Instance,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.clickable(onClick = {}).sizeIn(maxWidth = 220.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            HomeInstanceIcon(
                instance = instance,
                onLaunch = {},
                onStop = {},
                modifier = Modifier.fillMaxWidth()
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = instance.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = vectorResource(CoreDrawable.vercors),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = instance.loaderAndGameVersion,
                            //style = MaterialTheme.typography.subtitle2
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (statusIcon, statusText, statusColor) = when (instance.status) {
                            is HomeInstanceStatusUi.NotRunning -> Triple(
                                vectorResource(CoreDrawable.play),
                                stringResource(InstanceString.last_played, instance.status.lastPlayed),
                                Color.Unspecified
                            )

                            HomeInstanceStatusUi.Running -> Triple(
                                vectorResource(CoreDrawable.play),
                                stringResource(InstanceString.running),
                                MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = statusColor
                        )
                        Text(
                            text = statusText,
                            color = statusColor,
                            //style = MaterialTheme.typography.subtitle2
                        )
                    }
                }
            }
        }
    }
}