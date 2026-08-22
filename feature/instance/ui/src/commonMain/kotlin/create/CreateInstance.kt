package app.vercors.feature.instance.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.component.AppTextField
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.theme.*
import app.vercors.feature.game.ui.GameVersionSelector
import app.vercors.feature.game.ui.LoaderSelector
import app.vercors.feature.game.ui.LoaderVersionSelector
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.createInstance
import app.vercors.feature.instance.ui.instanceName
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.cancel
import app.vercors.shared.ui.create
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import com.composeunstyled.theme.Theme
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateInstance(
    store: Store<CreateInstanceState, CreateInstanceEvent>,
) {
    val (state, eventSink) = store

    AppDialog(onClickOutside = { eventSink(CreateInstanceEvent.OnCancelClick) }) {
        AppForm {
            AppText(
                text = stringResource(InstanceRes.string.createInstance),
                style = Theme[textStyles][title]
            )

            AppTextField(
                label = stringResource(InstanceRes.string.instanceName),
                initialValue = state.instanceName,
                onValueChange = { eventSink(CreateInstanceEvent.OnInstanceNameChange(it)) },
                modifier = Modifier.width(300.dp),
            )

            GameVersionSelector(
                selectedVersion = state.selectedGameVersion,
                versionList = state.gameVersionList.valueOrNull ?: emptyList(),
                onVersionChange = { eventSink(CreateInstanceEvent.OnGameVersionChange(it)) },
            )

            LoaderSelector(
                selectedLoader = state.selectedLoader,
                availableLoaders = state.loaderList.valueOrNull ?: emptyList(),
                onLoaderChange = { eventSink(CreateInstanceEvent.OnLoaderChange(it)) },
            )

            if (state.selectedLoader != null) {
                LoaderVersionSelector(
                    selectedVersion = state.selectedLoaderVersion,
                    versionList = state.loaderVersionList.valueOrNull ?: emptyList(),
                    onVersionChange = { eventSink(CreateInstanceEvent.OnLoaderVersionChange(it)) },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppIconTextButton(
                    onClick = { eventSink(CreateInstanceEvent.OnCancelClick) },
                    text = stringResource(SharedRes.string.cancel),
                    imageVector = Lucide.X,
                )
                AppIconTextButton(
                    onClick = { eventSink(CreateInstanceEvent.OnCreateInstanceClick) },
                    text = stringResource(SharedRes.string.create),
                    imageVector = Lucide.Plus,
                    backgroundColor = Theme[colors][primary],
                    contentColor = Theme[colors][onPrimary],
                    enabled = state.canCreateInstance
                )
            }
        }
    }
}
