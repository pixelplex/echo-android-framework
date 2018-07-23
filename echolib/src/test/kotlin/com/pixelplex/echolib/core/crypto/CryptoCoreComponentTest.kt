package com.pixelplex.echolib.core.crypto

import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.echolib.core.crypto.internal.CryptoCoreComponentImpl
import com.pixelplex.echolib.model.Address
import com.pixelplex.echolib.model.AuthorityType
import com.pixelplex.echolib.model.network.Network
import com.pixelplex.echolib.model.network.Testnet
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [CryptoCoreComponentImpl]
 *
 * @author Dmitriy Bushuev
 */
class CryptoCoreComponentTest {

    private lateinit var cryptoCoreComponent: CryptoCoreComponent

    private val name = "testName"
    private val password = "testPassword"
    private lateinit var network: Network
    private val authorityType = AuthorityType.ACTIVE

    @Before
    fun setUp() {
        network = Testnet()
        cryptoCoreComponent = CryptoCoreComponentImpl(network)
    }

    @Test
    fun addressEqualityTest() {
        val firstAddress = cryptoCoreComponent.getAddress(name, password, authorityType)
        val secondAddress = cryptoCoreComponent.getAddress(name, password, authorityType)

        assertEquals(firstAddress, secondAddress)

        val secondName = "secondTestName"
        val secondPassword = "secondTestPassword"

        val thirdAddress = cryptoCoreComponent.getAddress(secondName, secondPassword, authorityType)

        assertNotEquals(firstAddress, thirdAddress)

        val firstAccountAddress = Address(firstAddress, network)
        val secondAccountAddress = Address(thirdAddress, network)

        assertNotEquals(firstAccountAddress.pubKey, secondAccountAddress.pubKey)
    }

    @Test
    fun privateKeyTest() {
        val privateKey = cryptoCoreComponent.getPrivateKey(name, password, authorityType)

        assertTrue(ECKey.fromPrivate(privateKey).hasPrivKey())
    }

}