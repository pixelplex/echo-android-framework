package org.echo.mobile.framework.model

import com.google.common.primitives.Bytes
import org.echo.mobile.bitcoinj.Base58
import org.echo.mobile.framework.exception.MalformedAddressException
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.support.crypto.Checksum.CHECKSUM_SIZE
import org.echo.mobile.framework.support.crypto.Checksum.calculateChecksum

/**
 * Represents EcDSA address model in blockchain
 *
 * @author Daria Pechkovskaya
 */
class Address {

    var pubKey: PublicKey
        private set

    private val prefix: String

    constructor(pubKey: PublicKey) {
        this.pubKey = pubKey
        this.prefix = pubKey.network.addressPrefix
    }

    constructor(address: String, network: Network) {
        val prefixSize = network.addressPrefix.length
        this.prefix = address.substring(0..prefixSize)

        val decoded = Base58.decode(address.substring(prefixSize))
        val pubKey = decoded.copyOfRange(0, decoded.size - CHECKSUM_SIZE)
        this.pubKey = PublicKey(pubKey, network)

        val calculatedChecksum = calculateChecksum(pubKey)
        val checksum = decoded.copyOfRange(decoded.size - CHECKSUM_SIZE, decoded.size)

        for ((i, data) in calculatedChecksum.withIndex()) {
            if (checksum[i] != data) {
                throw MalformedAddressException("Address checksum error")
            }
        }
    }

    override fun toString(): String {
        val pubKey = pubKey.toBytes()
        val checksum = calculateChecksum(pubKey)
        val pubKeyChecksummed = Bytes.concat(pubKey, checksum)
        return this.prefix + Base58.encode(pubKeyChecksummed)
    }

    companion object {
        const val BITSHARES_PREFIX = "GPH"
        const val TESTNET_PREFIX = "TEST"
        const val DEVNET_PREFIX = "ECHO"
    }

}
