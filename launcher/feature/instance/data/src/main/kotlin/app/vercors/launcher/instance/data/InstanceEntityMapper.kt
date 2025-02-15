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

package app.vercors.launcher.instance.data

import app.vercors.launcher.game.data.toModLoaderType
import app.vercors.launcher.game.data.toStringData
import app.vercors.launcher.instance.domain.Instance
import app.vercors.launcher.instance.domain.InstanceModLoader

fun InstanceEntity.toInstance(): Instance {
    return Instance(
        id = id,
        slug = slug,
        name = name,
        gameVersion = gameVersion,
        modLoader = modLoader?.let {
            InstanceModLoader(
                type = modLoader.toModLoaderType(),
                version = modLoaderVersion!!
            )
        },
        createdAt = createdAt,
        lastPlayedAt = lastPlayedAt,
        playTime = playTime,
    )
}

fun Instance.toEntity(newSlug: String? = null): InstanceEntity {
    return InstanceEntity(
        id = id,
        slug = newSlug ?: slug,
        name = name,
        gameVersion = gameVersion,
        modLoader = modLoader?.type?.toStringData(),
        modLoaderVersion = modLoader?.version,
        createdAt = createdAt,
        lastPlayedAt = lastPlayedAt,
        playTime = playTime,
    )
}