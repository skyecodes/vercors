/*
 * Copyright (c) 2024 skyecodes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package app.vercors.launcher.core.meta

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.protobuf.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.*

@Module
@ComponentScan
class CoreMetaModule

@OptIn(ExperimentalSerializationApi::class)
@Single
@Named("metaKtorfit")
internal fun provideMetaKtorfit(
    httpClient: HttpClient,
    @Property("vercorsApiKey") apiKey: String,
    @Property("vercorsApiUrl") apiUrl: String
): Ktorfit = Ktorfit.Builder()
    .httpClient(httpClient.config {
        install(DefaultRequest) {
            bearerAuth(apiKey)
        }
        install(ContentNegotiation) {
            protobuf()
        }
    })
    .converterFactories(RemoteResultConverterFactory())
    .baseUrl(apiUrl.let { if (it.endsWith("/")) it else "$it/" })
    .build()