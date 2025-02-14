/*
 * Copyright (c) 2025 skyecodes
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

package app.vercors.launcher.instance.data

import app.vercors.launcher.core.storage.StorageService
import app.vercors.launcher.core.storage.deleteRecursively
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

@Single
@OptIn(ExperimentalSerializationApi::class)
class InstanceFileDataSource(
    private val storageService: StorageService,
    private val json: Json,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun scanInstances(): List<InstanceEntity> = coroutineScope {
        val instancesPath = Path(storageService.state.value.path, "instances")
        SystemFileSystem.list(instancesPath).map { instanceDir ->
            val instanceFile = Path(instanceDir, "instance.json")
            async(ioDispatcher) {
                if (SystemFileSystem.exists(instanceFile)) {
                    try {
                        SystemFileSystem.source(instanceFile).buffered()
                            .use { json.decodeFromSource<InstanceEntity>(it) }
                            .copy(slug = instanceDir.name)
                    } catch (e: Exception) {
                        ensureActive()
                        logger.warn(e) { "Failed to scan instance at location $instanceFile" }
                        null
                    }
                } else null
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun generateSlug(instanceName: String): String {
        val instancesPath = Path(storageService.state.value.path, "instances")
        val baseSlug = instanceName.replace(Regex("[\"\';:/\\\\]"), "")
        return withContext(ioDispatcher) {
            var slug = baseSlug
            var i = 1
            while (SystemFileSystem.exists(Path(instancesPath, baseSlug)))
                slug = "${baseSlug}_${i++}"
            slug
        }
    }

    suspend fun saveInstance(instance: InstanceEntity) {
        val instanceDir = Path(storageService.state.value.path, "instances", instance.slug)
        withContext(ioDispatcher) {
            SystemFileSystem.createDirectories(instanceDir)
            val instanceFile = Path(instanceDir, "instance.json")
            SystemFileSystem.sink(instanceFile).buffered().use { json.encodeToSink(instance, it) }
        }
    }

    suspend fun deleteInstance(instance: InstanceEntity) {
        val instanceDir = Path(storageService.state.value.path, "instances", instance.slug)
        withContext(ioDispatcher) {
            SystemFileSystem.deleteRecursively(instanceDir)
        }
    }
}