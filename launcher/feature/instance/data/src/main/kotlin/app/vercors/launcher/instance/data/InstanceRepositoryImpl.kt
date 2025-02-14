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

package app.vercors.launcher.instance.data

import app.vercors.launcher.instance.domain.Instance
import app.vercors.launcher.instance.domain.InstanceRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger { }

@Single
class InstanceRepositoryImpl(
    private val dao: InstanceDao,
    private val fileDataSource: InstanceFileDataSource,
    private val externalScope: CoroutineScope,
) : InstanceRepository {
    init {
        synchronize()
    }

    override fun synchronize() {
        externalScope.launch {
            val (dbInstances, fileInstances) = listOf(
                async { dao.observeInstances().first() },
                async { fileDataSource.scanInstances() }
            ).awaitAll()
            fileInstances.filter { it.slug !in dbInstances.map { it.slug } }.forEach {
                logger.info { "${it.slug} instance found - adding to database" }
                dao.insert(it)
            }
            dbInstances.filter { it.slug !in fileInstances.map { it.slug } }.forEach {
                logger.warn { "${it.slug} instance no longer on disk - removing from database" }
                dao.delete(it)
            }
        }
    }

    override fun observeAll(): Flow<List<Instance>> = dao.observeInstances()
        .map { instances -> instances.map { it.toInstance() } }
        .distinctUntilChanged()

    override fun observeById(id: Int): Flow<Instance> = dao.observeInstanceById(id)
        .map { it.toInstance() }
        .distinctUntilChanged()

    override suspend fun create(instance: Instance) {
        val entity = instance.toEntity(fileDataSource.generateSlug(instance.name))
        dao.insert(entity)
        fileDataSource.saveInstance(entity)
    }

    override suspend fun update(instance: Instance) {
        val entity = instance.toEntity()
        dao.update(entity)
        fileDataSource.saveInstance(entity)
    }

    override suspend fun delete(instance: Instance) {
        val entity = instance.toEntity()
        dao.delete(entity)
        fileDataSource.deleteInstance(entity)
    }
}