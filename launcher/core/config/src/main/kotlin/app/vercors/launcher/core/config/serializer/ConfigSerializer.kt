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

package app.vercors.launcher.core.config.serializer

import androidx.datastore.core.Serializer
import app.vercors.launcher.core.config.mapper.toProto
import app.vercors.launcher.core.config.model.AppConfig
import app.vercors.launcher.core.config.proto.ConfigProto
import org.koin.core.annotation.Single
import java.io.InputStream
import java.io.OutputStream

@Single
class ConfigSerializer : Serializer<ConfigProto> {
    override val defaultValue: ConfigProto = AppConfig.DEFAULT.toProto()
    override suspend fun readFrom(input: InputStream): ConfigProto = ConfigProto.parseFrom(input)
    override suspend fun writeTo(t: ConfigProto, output: OutputStream) = t.writeTo(output)
}