package app.vercors.launcher.common.data.account

import app.vercors.launcher.common.data.Sha256
import app.vercors.launcher.common.domain.account.AuthenticationException
import app.vercors.launcher.common.domain.account.AuthenticationService
import app.vercors.launcher.common.domain.account.model.Account
import app.vercors.launcher.common.domain.account.model.LoginState
import app.vercors.launcher.common.domain.account.model.LoginStep
import dev.whyoleg.cryptography.operations.Hasher
import dev.whyoleg.cryptography.random.CryptographyRandom
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

private val logger = KotlinLogging.logger {}

@Single
internal class AuthenticationServiceImpl(
    private val httpClient: HttpClient,
    @Property("MICROSOFT_CLIENT_ID") private val clientId: String,
    @Sha256 private val sha256Hasher: Hasher,
) : AuthenticationService {
    override fun login(): Flow<LoginState> = flow {
        val encoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
        val state = Uuid.random().toString()
        val codeVerifier = encoder.encode(CryptographyRandom.nextBytes(64))
        val codeChallenge = encoder.encode(sha256Hasher.hash(codeVerifier.encodeToByteArray()))
        val (authCode, port, reponseState) =
            getAuthorizationCode(state, codeChallenge).transform {
                when (it) {
                    is AuthorizationCodeResult.WaitingForLogin -> this@flow.emit(LoginState.RequireLogin(it.url))
                    is AuthorizationCodeResult.Success -> emit(it)
                    is AuthorizationCodeResult.Error -> it.throwException()
                }
            }.first()
        check(state == reponseState) { "State mismatch" }
        emit(LoginState.Progress(LoginStep.MicrosoftToken))
        val (msAccessToken, refreshToken) = getMicrosoftAccessToken(codeVerifier, authCode, port)
        runAuthProcess(msAccessToken, refreshToken)
    }

    override suspend fun verifyToken(accessToken: String): Boolean {
        try {
            getMinecraftProfile(accessToken)
            return true
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.warn(e) { "Failed to verify token" }
            return false
        }
    }

    override fun refreshToken(refreshToken: String): Flow<LoginState> = flow {
        emit(LoginState.Progress(LoginStep.MicrosoftToken))
        val (msAccessToken, refreshToken) = refreshMicrosoftAccessToken(refreshToken)
        runAuthProcess(msAccessToken, refreshToken)
    }

    private suspend fun FlowCollector<LoginState>.runAuthProcess(
        msAccessToken: String,
        refreshToken: String
    ) {
        emit(LoginState.Progress(LoginStep.XBLToken))
        val (xblToken, userHash) = getXblToken(msAccessToken)
        emit(LoginState.Progress(LoginStep.XSTSToken))
        val xstsToken = getXstsToken(xblToken, userHash)
        emit(LoginState.Progress(LoginStep.MinecraftToken))
        val mcAccessToken = getMinecraftAccessToken(userHash, xstsToken)
        emit(LoginState.Progress(LoginStep.MinecraftProfile))
        val (uuid, username) = getMinecraftProfile(mcAccessToken)
        emit(LoginState.Progress(LoginStep.Success))
        val account = Account(
            uuid = Uuid.parse(uuid),
            username = username,
            accessToken = mcAccessToken,
            refreshToken = refreshToken
        )
        emit(LoginState.Success(account))
    }

    private suspend fun getAuthorizationCode(
        state: String,
        codeChallenge: String
    ): Flow<AuthorizationCodeResult> = channelFlow {
        logger.info { "Getting authorization code" }
        val awaitJob = Job()
        val server = embeddedServer(CIO, 0) {
            routing {
                get("/") {
                    if (call.queryParameters.contains("code")) {
                        val authorizationCode = call.queryParameters["code"]!!
                        val responseState = call.queryParameters["state"]!!
                        call.respondText("Authentication success! You can now close this window.")
                        send(AuthorizationCodeResult.Success(authorizationCode, engine.port(), responseState))
                    } else if (call.queryParameters.contains("error")) {
                        val errorCode = call.queryParameters["error"]!!
                        val errorDescription = call.queryParameters["error_description"]!!
                        call.respondText("$errorCode: $errorDescription")
                        send(AuthorizationCodeResult.Error(errorCode, errorDescription))
                    } else {
                        call.respondText("Error: Authentication failed")
                        send(AuthorizationCodeResult.Error("Error", "Authentication failed"))
                    }
                    awaitJob.complete()
                }
            }
        }
        server.startSuspend()
        val port = server.engine.port()
        val url = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?" +
                "client_id=$clientId" +
                "&response_type=code" +
                "&redirect_uri=http%3A%2F%2Flocalhost%3A$port" +
                "&response_mode=query" +
                "&scope=XboxLive.signin%20offline_access" +
                "&state=$state" +
                "&prompt=select_account" +
                "&code_challenge=$codeChallenge" +
                "&code_challenge_method=S256"
        send(AuthorizationCodeResult.WaitingForLogin(url))
        awaitJob.join()
        server.stopSuspend()
    }

    private suspend fun getMicrosoftAccessToken(
        codeVerifier: String,
        authorizationCode: String,
        port: Int
    ): MicrosoftAccessTokenResult {
        logger.info { "Getting access token" }
        return getMicrosoftAccessTokenWithParams(
            "code" to authorizationCode,
            "redirect_uri" to "http://localhost:$port",
            "grant_type" to "authorization_code",
            "code_verifier" to codeVerifier,
        )
    }

    private suspend fun refreshMicrosoftAccessToken(refreshToken: String): MicrosoftAccessTokenResult {
        logger.info { "Refreshing access token" }
        return getMicrosoftAccessTokenWithParams(
            "refresh_token" to refreshToken,
            "grant_type" to "refresh_token"
        )
    }

    private suspend fun getMicrosoftAccessTokenWithParams(vararg params: Pair<String, String>): MicrosoftAccessTokenResult {
        val tokenResult = httpClient.submitForm(
            url = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token",
            formParameters = parameters {
                set("client_id", clientId)
                set("scope", "XboxLive.signin offline_access")
                params.forEach { (a, b) -> set(a, b) }
            }
        ).body<JsonObject>()
        val xblAccessToken = tokenResult["access_token"]?.string
        val refreshToken = tokenResult["refresh_token"]?.string
        if (xblAccessToken == null || refreshToken == null) {
            if (tokenResult.containsKey("error")) {
                val error = tokenResult.getValue("error").string
                val errorDescription = tokenResult.getValue("error_description").string
                throw AuthResponseException(error, errorDescription)
            } else {
                invalidResponse(tokenResult)
            }
        }
        return MicrosoftAccessTokenResult(xblAccessToken, refreshToken)
    }

    private suspend fun getXblToken(xblAccessToken: String): XblTokenResult {
        logger.info { "Authenticating with Xbox Live" }
        val xboxResult = httpClient.post("https://user.auth.xboxlive.com/user/authenticate") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("Properties", buildJsonObject {
                    put("AuthMethod", "RPS")
                    put("SiteName", "user.auth.xboxlive.com")
                    put("RpsTicket", "d=$xblAccessToken")
                })
                put("RelyingParty", "http://auth.xboxlive.com")
                put("TokenType", "JWT")
            })
        }.body<JsonObject>()
        val xblToken = xboxResult["Token"]?.string
        val userHash = xboxResult["DisplayClaims"]?.jsonObject?.get("xui")
            ?.jsonArray?.get(0)?.jsonObject?.get("uhs")?.string
        if (xblToken == null || userHash == null) invalidResponse(xboxResult)
        return XblTokenResult(xblToken, userHash)
    }

    private suspend fun getXstsToken(xblToken: String, userHash: String): String {
        logger.info { "Obtaining XSTS token" }
        val xstsResult = httpClient.post("https://xsts.auth.xboxlive.com/xsts/authorize") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("Properties", buildJsonObject {
                    put("SandboxId", "RETAIL")
                    put("UserTokens", buildJsonArray { add(xblToken) })
                })
                put("RelyingParty", "rp://api.minecraftservices.com/")
                put("TokenType", "JWT")
            })
        }.body<JsonObject>()
        val xstsToken = xstsResult["Token"]?.string
        val userHash2 = xstsResult["DisplayClaims"]?.jsonObject?.get("xui")
            ?.jsonArray?.get(0)?.jsonObject?.get("uhs")?.string

        if (xstsToken == null) invalidResponse(xstsResult)
        if (userHash != userHash2) throw AuthenticationException("User hashes don't match")
        return xstsToken
    }

    private suspend fun getMinecraftAccessToken(userHash: String, xstsToken: String): String {
        logger.info { "Authenticating with Minecraft" }
        val minecraftResult = httpClient.post("https://api.minecraftservices.com/authentication/login_with_xbox") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("identityToken", "XBL3.0 x=$userHash;$xstsToken")
            })
        }.body<JsonObject>()
        val mcAccessToken = minecraftResult["access_token"]?.string
            ?: if (minecraftResult.containsKey("errorMessage")) throw AuthenticationException(minecraftResult["errorMessage"]?.string)
            else invalidResponse(minecraftResult)
        return mcAccessToken
    }

    private suspend fun getMinecraftProfile(mcAccessToken: String): MinecraftProfileResult {
        logger.info { "Getting Minecraft profile" }
        val profileResult = httpClient.get("https://api.minecraftservices.com/minecraft/profile") {
            bearerAuth(mcAccessToken)
            accept(ContentType.Application.Json)
        }.body<JsonObject>()
        val uuid = profileResult["id"]?.string
        val username = profileResult["name"]?.string
        if (uuid == null || username == null) invalidResponse(profileResult)
        return MinecraftProfileResult(uuid, username)
    }

    private fun invalidResponse(result: JsonObject): Nothing {
        logger.warn { "Invalid response: $result" }
        throw AuthenticationException("Invalid response, check logs for details")
    }

    private suspend fun ApplicationEngine.port() = resolvedConnectors()[0].port

    private val JsonElement.string get() = jsonPrimitive.content

    private sealed interface AuthorizationCodeResult {
        data class WaitingForLogin(val url: String) : AuthorizationCodeResult
        data class Success(val authorizationCode: String, val port: Int, val responseState: String) :
            AuthorizationCodeResult

        data class Error(val error: String, val description: String) : AuthorizationCodeResult {
            fun throwException(): Nothing = throw AuthResponseException(error, description)
        }
    }

    private class AuthResponseException(error: String, description: String) :
        AuthenticationException("$error: $description")

    private data class MicrosoftAccessTokenResult(val msAccessToken: String, val refreshToken: String)

    private data class MinecraftProfileResult(val uuid: String, val username: String)

    private data class XblTokenResult(val xblToken: String, val userHash: String)
}
