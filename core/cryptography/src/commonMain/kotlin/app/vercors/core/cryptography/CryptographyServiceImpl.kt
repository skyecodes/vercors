@file:OptIn(CryptographyProviderApi::class, DelicateCryptographyApi::class)

package app.vercors.core.cryptography

import dev.whyoleg.cryptography.BinarySize.Companion.bits
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.SHA1
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.random.CryptographyRandom
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.withContext
import kotlinx.io.RawSource
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.encodeToByteString
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmInline

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class CryptographyServiceImpl : CryptographyService {
    private val sha1 by lazy { CryptographyProvider.Default.get(SHA1).hasher() }
    private val sha256 by lazy { CryptographyProvider.Default.get(SHA256).hasher() }
    private val aecGcm by lazy { CryptographyProvider.Default.get(AES.GCM) }
    private val aecGcmKeyGenerator by lazy { aecGcm.keyGenerator(AES.Key.Size.B256) }
    private val aecGcmKeyDecoder by lazy { aecGcm.keyDecoder() }

    override suspend fun randomBytes(size: Int): ByteArray =
        CryptographyRandom.nextBytes(size)

    override suspend fun sha1(
        source: RawSource,
        context: CoroutineContext
    ): ByteString = withContext(context) {
        sha1.hash(source)
    }

    override suspend fun sha1(
        data: ByteArray,
        context: CoroutineContext
    ): ByteArray = withContext(context) {
        sha1.hash(data)
    }

    override suspend fun sha256(
        source: RawSource,
        context: CoroutineContext
    ): ByteString = withContext(context) {
        sha256.hash(source)
    }

    override suspend fun sha256(
        data: ByteArray,
        context: CoroutineContext
    ): ByteArray = withContext(context) {
        sha256.hash(data)
    }

    override suspend fun generateKey(
        context: CoroutineContext
    ): CryptographicKey = withContext(context) {
        CryptographicKeyImpl(aecGcmKeyGenerator.generateKey())
    }

    override suspend fun encodeKey(
        key: CryptographicKey,
        context: CoroutineContext
    ): ByteString = withContext(context) {
        (key as CryptographicKeyImpl).encodeToByteString(AES.Key.Format.RAW)
    }

    override suspend fun decodeKey(
        keyData: ByteString,
        context: CoroutineContext
    ): CryptographicKey = withContext(context) {
        CryptographicKeyImpl(aecGcmKeyDecoder.decodeFromByteString(AES.Key.Format.RAW, keyData))
    }

    override suspend fun encode(
        key: CryptographicKey,
        plainData: String,
        context: CoroutineContext
    ): ByteString = withContext(context) {
        (key as CryptographicKeyImpl).cipher(128.bits).encrypt(plainData.encodeToByteString())
    }

    override suspend fun decode(
        key: CryptographicKey,
        encryptedData: ByteString,
        context: CoroutineContext
    ): String = withContext(context) {
        (key as CryptographicKeyImpl).cipher(128.bits).decrypt(encryptedData).decodeToString()
    }
}

@JvmInline
internal value class CryptographicKeyImpl(private val key: AES.GCM.Key) : CryptographicKey, AES.GCM.Key by key
