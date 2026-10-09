package app.vercors.meta

import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@ContributesTo(AppScope::class)
@BindingContainer
object MetaBackModule {
    @SingleIn(AppScope::class)
    @Provides
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }


    @SingleIn(AppScope::class)
    @Provides
    fun provideHttpClient(json: Json): HttpClient = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpCache)
        install(UserAgent) {
            agent = "skyecodes/vercors-meta/$VERSION (contact@skye.codes)"
        }
    }

    @Provides
    fun provideKtorfitBuilder(httpClient: HttpClient): Ktorfit.Builder = Ktorfit.Builder().httpClient(httpClient)
}
