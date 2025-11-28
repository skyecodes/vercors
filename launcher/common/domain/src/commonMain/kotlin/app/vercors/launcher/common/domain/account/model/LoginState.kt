package app.vercors.launcher.common.domain.account.model

sealed interface LoginState {
    data class RequireLogin(val url: String) : LoginState
    data class Progress(val step: LoginStep) : LoginState
    data class Success(val account: Account) : LoginState
    data class Error(val error: Exception) : LoginState

    fun throwIfError() = if (this is Error) throw error else this
}

enum class LoginStep(val number: Int, val description: String) {
    MicrosoftToken(1, "Getting Microsoft access token"),
    XBLToken(2, "Getting XBL token"),
    XSTSToken(3, "Getting XSTS token"),
    MinecraftToken(4, "Getting Minecraft access token"),
    MinecraftProfile(5, "Getting Minecraft profile"),
    Success(6, "Login successful"),
}