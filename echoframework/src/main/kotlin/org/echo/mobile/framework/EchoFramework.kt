package org.echo.mobile.framework

import org.echo.mobile.framework.facade.*
import org.echo.mobile.framework.service.*
import org.echo.mobile.framework.support.Settings

/**
 * Describes base library functionality
 *
 * Represents base library facade,
 * that combines all other facades to provide the only entry point of library
 *
 * @author Dmitriy Bushuev
 */
interface EchoFramework : AuthenticationFacade, FeeFacade, InformationFacade, SubscriptionFacade,
    TransactionsFacade, AssetsFacade, ContractsFacade {

    /**
     * Current library instance of [AccountHistoryApiService]
     */
    val accountHistoryApiService: AccountHistoryApiService

    /**
     * Current library instance of [DatabaseApiService]
     */
    val databaseApiService: DatabaseApiService

    /**
     * Current library instance of [NetworkBroadcastApiService]
     */
    val networkBroadcastApiService: NetworkBroadcastApiService

    /**
     * Current library instance of [CryptoApiService]
     */
    val cryptoApiService: CryptoApiService

    /**
     * Current library instance of [LoginApiService]
     */
    val loginService: LoginApiService

    /**
     * Starts socket connection, connects to blockchain apis
     */
    fun start(callback: Callback<Any>)

    /**
     * Stops socket connection, unsubscribe all listeners
     */
    fun stop()

    companion object {

        /**
         * Creates library with settings
         * @param settings Settings for initialization
         */
        fun create(settings: Settings): EchoFramework = EchoFrameworkImpl(settings)
    }

}
