package app.vercors.meta.backend

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
class MetaBackModule

@Single
fun provideJson(): Json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    isLenient = true
}

@Single
fun provideHttpClient(json: Json): HttpClient = HttpClient {
    expectSuccess = true
    install(ContentNegotiation) {
        json(json)
    }
    install(HttpCache)
    install(UserAgent) {
        agent = "skyecodes/vercors/$VERSION (contact@skye.codes)"
    }
}

@Factory
fun provideKtorfitBuilder(httpClient: HttpClient): Ktorfit.Builder = Ktorfit.Builder().httpClient(httpClient)
