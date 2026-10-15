package app.vercors.feature.instance.ui.details.launch

import androidx.compose.runtime.Immutable
import app.vercors.feature.account.ui.list.LoginDialog
import app.vercors.feature.instance.ui.details.InstallJavaProgressDialog

@Immutable
data class InstanceLaunchState(
    val promptInstallJava: Int?,
    val promptLogin: Boolean,
    val loginDialog: LoginDialog,
    val promptJavaInstallProgress: Boolean,
    val installJavaProgressDialog: InstallJavaProgressDialog,
)
