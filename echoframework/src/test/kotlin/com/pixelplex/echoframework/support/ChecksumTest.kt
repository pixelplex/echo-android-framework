package com.pixelplex.echoframework.support

import com.pixelplex.bitcoinj.Base58
import com.pixelplex.echoframework.support.crypto.Checksum
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test cases for [Checksum]
 *
 * @author Dmitriy Bushuev
 */
class ChecksumTest {

    @Test
    fun checksumLengthTest() {
        val bytes = Base58.decode("JxF12TrwUP45BMd")

        val hashSizeInBytes = Checksum.CHECKSUM_SIZE
        val hash = Checksum.calculateChecksum(bytes)

        assertTrue(hash.size == hashSizeInBytes)
    }

}
