package app.vercors.core.cryptography

import kotlinx.coroutines.Dispatchers
import kotlinx.io.RawSource
import kotlinx.io.bytestring.ByteString
import kotlin.coroutines.CoroutineContext

interface CryptographyService {
    suspend fun randomBytes(size: Int): ByteArray
    suspend fun sha1(source: RawSource, context: CoroutineContext = Dispatchers.Default): ByteString
    suspend fun md5(source: RawSource, context: CoroutineContext = Dispatchers.Default): ByteString
    suspend fun sha256(data: ByteArray, context: CoroutineContext = Dispatchers.Default): ByteArray
    suspend fun generateKey(context: CoroutineContext = Dispatchers.Default): CryptographicKey
    suspend fun encodeKey(key: CryptographicKey, context: CoroutineContext = Dispatchers.Default): ByteString
    suspend fun decodeKey(keyData: ByteString, context: CoroutineContext = Dispatchers.Default): CryptographicKey
    suspend fun encode(
        key: CryptographicKey,
        plainData: String,
        context: CoroutineContext = Dispatchers.Default
    ): ByteString

    suspend fun decode(
        key: CryptographicKey,
        encryptedData: ByteString,
        context: CoroutineContext = Dispatchers.Default
    ): String
}

sealed interface CryptographicKey
