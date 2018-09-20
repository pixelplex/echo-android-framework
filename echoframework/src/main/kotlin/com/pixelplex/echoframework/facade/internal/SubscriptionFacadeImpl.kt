package com.pixelplex.echoframework.facade.internal

import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent
import com.pixelplex.echoframework.core.socket.SocketCoreComponent
import com.pixelplex.echoframework.core.socket.SocketMessengerListener
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.facade.SubscriptionFacade
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.FullAccount
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.service.AccountSubscriptionManager
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.service.internal.AccountSubscriptionManagerImpl
import com.pixelplex.echoframework.support.*
import com.pixelplex.echoframework.support.concurrent.future.FutureTask
import com.pixelplex.echoframework.support.concurrent.future.completeCallback
import com.pixelplex.echoframework.support.concurrent.future.wrapResult

/**
 * Implementation of [SubscriptionFacade]
 *
 * @author Dmitriy Bushuev
 */
class SubscriptionFacadeImpl(
    private val socketCoreComponent: SocketCoreComponent,
    private val databaseApiService: DatabaseApiService,
    private val network: Network
) : SubscriptionFacade {

    private val socketMessengerListener by lazy {
        SubscriptionListener()
    }

    @Volatile
    private var subscribed = false

    private val subscriptionManager: AccountSubscriptionManager by lazy {
        AccountSubscriptionManagerImpl(network)
    }

    override fun subscribeOnAccount(
        nameOrId: String,
        listener: AccountListener,
        callback: Callback<Boolean>
    ) {
        synchronized(this) {
            if (!subscribed) {
                socketCoreComponent.on(socketMessengerListener)

                this.subscribed = subscribeCallBlocking()
                if (!subscribed) {
                    callback.onError(LocalException("Subscription request error"))
                }
            }

            if (!subscriptionManager.registered(nameOrId)) {
                getAccountId(nameOrId)
                    .flatMap { account ->
                        databaseApiService.getFullAccounts(listOf(account), true)
                    }
                    .value { accountsMap ->
                        accountsMap.values.firstOrNull()?.account?.getObjectId()?.let { id ->
                            subscriptionManager.registerListener(id, listener)
                            callback.onSuccess(subscribed)
                        }
                    }
                    .error { error ->
                        LOGGER.log("Account finding error.", error)
                        callback.onError(LocalException(error))
                    }
            } else {
                subscriptionManager.registerListener(nameOrId, listener)
                callback.onSuccess(subscribed)
            }
        }
    }

    private fun subscribeCallBlocking(): Boolean {
        val futureResult = FutureTask<Boolean>()

        databaseApiService.subscribe(true, futureResult.completeCallback())

        var result = false

        futureResult.wrapResult<Exception, Boolean>(false)
            .value {
                result = it
            }
            .error { error ->
                LOGGER.log("Subscription request error", error)
                result = false
            }

        return result
    }

    override fun unsubscribeFromAccount(nameOrId: String, callback: Callback<Boolean>) {
        // if there are listeners registered with [nameOrId] - remove them
        // else - request account by [nameOrId] and try remove listeners by account's id
        if (!subscriptionManager.registered(nameOrId)) {
            synchronized(this) {
                if (!subscriptionManager.registered(nameOrId)) {
                    getAccountId(nameOrId)
                        .map { id -> subscriptionManager.removeListeners(id) }
                        .value { existedListeners ->
                            if (existedListeners != null) {
                                callback.onSuccess(true)
                            } else {
                                LOGGER.log("No listeners found for this account")
                                callback.onError(LocalException("No listeners found for this account"))
                            }
                        }
                        .error { error ->
                            LOGGER.log("Account finding error.", error)
                            callback.onError(error)
                        }
                }
            }
        } else {
            subscriptionManager.removeListeners(nameOrId)
            callback.onSuccess(true)
        }
    }

    override fun unsubscribeAll(callback: Callback<Boolean>) {
        synchronized(this) {
            if (subscribed) {
                cancelAllSubscriptions()
                    .value { result ->
                        subscribed = !result

                        socketCoreComponent.off(socketMessengerListener)
                        subscriptionManager.clear()
                        callback.onSuccess(result)
                    }
                    .error { error ->
                        callback.onError(error)
                    }
            } else {
                callback.onSuccess(true)
            }
        }
    }

    private fun cancelAllSubscriptions(): Result<LocalException, Boolean> {
        val future = FutureTask<Boolean>()
        databaseApiService.unsubscribe(future.completeCallback())

        return future.wrapResult(false)
    }

    private fun getAccountId(nameOrId: String): Result<LocalException, String> =
        databaseApiService.getFullAccounts(listOf(nameOrId), false)
            .flatMap { accountsMap ->
                accountsMap[nameOrId]?.account?.getObjectId()?.let { Result.Value(it) }
                    ?: Result.Error(LocalException())
            }
            .mapError {
                LocalException("Unable to find required account id for identifier = $nameOrId")
            }

    private fun getAccount(nameOrId: String): Result<LocalException, Account> =
        databaseApiService.getFullAccounts(listOf(nameOrId), false)
            .flatMap { accountsMap ->
                accountsMap[nameOrId]?.account?.let { Result.Value(it) }
                    ?: Result.Error(LocalException())
            }
            .mapError {
                LocalException("Unable to find required account id for identifier = $nameOrId")
            }

    private fun resetState() {
        subscriptionManager.clear()
        socketCoreComponent.off(socketMessengerListener)
    }

    private inner class SubscriptionListener : SocketMessengerListener {

        override fun onEvent(event: String) {
            // no need to process other events
            if (event.toJsonObject()?.get(METHOD_KEY)?.asString != NOTICE_METHOD_KEY) {
                return
            }

            val accountIds = subscriptionManager.processEvent(event) ?: return

            databaseApiService.getFullAccounts(
                accountIds,
                false,
                FullAccountSubscriptionCallback(accountIds)
            )
        }

        override fun onFailure(error: Throwable) = resetState()

        override fun onConnected() {
        }

        override fun onDisconnected() = resetState()

        private inner class FullAccountSubscriptionCallback(private val accountIds: List<String>) :
            Callback<Map<String, FullAccount>> {
            override fun onSuccess(result: Map<String, FullAccount>) {
                accountIds.forEach { accountId ->
                    val account = result[accountId] ?: return

                    subscriptionManager.notify(account)
                }
            }

            override fun onError(error: LocalException) {
            }

        }
    }

    companion object {
        const val METHOD_KEY = "method"
        private const val NOTICE_METHOD_KEY = "notice"

        private val LOGGER = LoggerCoreComponent.create(SubscriptionFacadeImpl::class.java.name)
    }

}
