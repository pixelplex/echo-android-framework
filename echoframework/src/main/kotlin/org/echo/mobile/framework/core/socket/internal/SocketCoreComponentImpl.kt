package org.echo.mobile.framework.core.socket.internal

import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.core.mapper.MapperCoreComponent
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.core.socket.SocketMessenger
import org.echo.mobile.framework.core.socket.SocketMessengerListener
import org.echo.mobile.framework.core.socket.SocketState
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.ResponseSocketException
import org.echo.mobile.framework.exception.SocketConnectionException
import org.echo.mobile.framework.model.SocketResponse
import org.echo.mobile.framework.model.socketoperations.SocketOperation
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Implementation of [SocketCoreComponent]
 *
 * @author Daria Pechkovskaya
 */
class SocketCoreComponentImpl(
    private val socketMessenger: SocketMessenger,
    private val mapper: MapperCoreComponent
) : SocketCoreComponent {

    private var callIdx = AtomicInteger(INITIAL_ID)

    private val operationsMap: ConcurrentHashMap<Int, SocketOperation<Any>> = ConcurrentHashMap()

    private val globalSocketListener = SocketCoreMessengerListener()

    override var socketState: SocketState = SocketState.DISCONNECTED

    override val currentId: Int
        get() = callIdx.getAndIncrement()

    override fun connect(url: String) = with(socketMessenger) {
        on(globalSocketListener)
        setUrl(url)
        connect()
    }

    override fun disconnect() = socketMessenger.disconnect()

    @Suppress("unchecked_cast")
    override fun emit(operation: SocketOperation<*>) {
        operationsMap.putIfAbsent(operation.callId, operation as SocketOperation<Any>)

        socketMessenger.emit(operation.toJsonString() ?: "")
    }

    override fun on(listener: SocketMessengerListener) = socketMessenger.on(listener)

    override fun off(listener: SocketMessengerListener) = socketMessenger.off(listener)

    private inner class SocketCoreMessengerListener : SocketMessengerListener {

        override fun onEvent(event: String) {
            val response = mapper.map(event, SocketResponse::class.java) ?: return

            val operation = operationsMap.remove(response.id)
            val error = response.error

            operation?.let { notNullOperation ->
                error?.let { notNullError ->
                    with(ResponseSocketException(notNullError.message)) {
                        LOGGER.log("Response error. Response = $event", this)
                        notNullOperation.callback.onError(this)
                    }
                } ?: mapData(event, notNullOperation)
            }
        }

        private fun mapData(event: String, operation: SocketOperation<Any>) {
            try {
                val obj = operation.fromJson(event)!!
                operation.callback.onSuccess(obj)
            } catch (ex: Exception) {
                LOGGER.log("Error during response mapping. Response = $event", ex)
                operation.callback.onError(LocalException(ex.message, ex))
            }
        }

        override fun onConnected() {
            socketState = SocketState.CONNECTED
        }

        override fun onFailure(error: Throwable) {
            notifyDisconnected()
        }

        override fun onDisconnected() {
            notifyDisconnected()
        }

        private fun notifyDisconnected() {
            if (socketState == SocketState.DISCONNECTED) return

            socketState = SocketState.DISCONNECTED

            //event to all active operations about disconnection
            val error = SocketConnectionException("Socket is disconnected.")
            operationsMap.forEach { (_, operation) ->
                operation.callback.onError(error)
            }
            operationsMap.clear()
            callIdx.set(INITIAL_ID)

            //if has required options - reconnect by delay
        }

    }

    companion object {
        private val LOGGER =
            LoggerCoreComponent.create(SocketCoreComponentImpl::class.java.name)

        private const val INITIAL_ID = 1
    }

}
