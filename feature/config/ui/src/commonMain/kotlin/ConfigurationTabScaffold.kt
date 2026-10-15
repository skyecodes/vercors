package app.vercors.feature.config.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.modifier.appCard

@Composable
internal fun ConfigurationTabScaffold(
    horizontalTabs: Boolean?,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier.let { if (horizontalTabs == false) it.padding(vertical = 8.dp) else it }.appCard()
                .padding(8.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}
