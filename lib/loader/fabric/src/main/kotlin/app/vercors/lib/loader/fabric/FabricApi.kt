/*
 * Copyright (c) 2024-2025 skyecodes
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

package app.vercors.lib.loader.fabric

import app.vercors.lib.domain.RemoteResult
import app.vercors.lib.loader.fabriclike.FabricLikeVersions
import app.vercors.lib.network.RemoteResultConverterFactory
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.http.GET
import io.ktor.client.*

@Suppress("kotlin:S6517")
interface FabricApi {
    @GET("v2/versions")
    suspend fun getAllVersions(): RemoteResult<FabricLikeVersions>

    companion object {
        const val BASE_URL = "https://meta.fabricmc.net/"
    }
}

fun FabricApi(
    httpClient: HttpClient,
    baseUrl: String = FabricApi.BASE_URL
): FabricApi = Ktorfit.Builder()
    .baseUrl(baseUrl)
    .httpClient(httpClient)
    .converterFactories(RemoteResultConverterFactory)
    .build()
    .createFabricApi()