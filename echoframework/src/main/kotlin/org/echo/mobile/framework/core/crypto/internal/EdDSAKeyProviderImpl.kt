package org.echo.mobile.framework.core.crypto.internal

import org.echo.mobile.bitcoinj.Base58
import org.echo.mobile.framework.core.crypto.EdDSAKeyProvider
import org.echo.mobile.framework.core.crypto.internal.EdDSAKeyProviderImpl.Companion.EDDSA_KEY_PREFIX
import org.echo.mobile.framework.core.crypto.internal.eddsa.key.KeyPairCryptoAdapter
import org.echo.mobile.framework.exception.LocalException

/**
 * [EdDSAKeyProvider] implementation based on realisations of [KeyPairCryptoAdapter]
 *
 * Returns public key of generated key pair encoded in base58. Added specified [prefix].
 * Default prefix - [EDDSA_KEY_PREFIX]
 *
 * Wraps all errors in [LocalException]
 *
 * @author Dmitriy Bushuev
 */
class EdDSAKeyProviderImpl(
    private val keyPairCryptoAdapter: KeyPairCryptoAdapter,
    private val prefix: String = EDDSA_KEY_PREFIX
) :
    EdDSAKeyProvider {

    override fun provideAddress(seed: ByteArray): String =
        wrapError { prefix + Base58.encode(keyPairCryptoAdapter.keyPair(seed).first) }

    override fun provideAddress(): String =
        wrapError { prefix + Base58.encode(keyPairCryptoAdapter.keyPair().first) }

    override fun providePublicKeyRaw(seed: ByteArray): ByteArray =
        wrapError { keyPairCryptoAdapter.keyPair(seed).first }

    override fun providePrivateKeyRaw(seed: ByteArray): ByteArray =
        wrapError { keyPairCryptoAdapter.keyPair(seed).second }

    private fun <T> wrapError(body: () -> T): T =
        try {
            body()
        } catch (exception: Exception) {
            throw LocalException("Error occurred during EdDSA key generation", exception)
        }

    override fun provideAddressFromPublicKey(key: ByteArray): String =
        wrapError { prefix + Base58.encode(key) }

    companion object {
        private const val EDDSA_KEY_PREFIX = "ECHO"
    }

}