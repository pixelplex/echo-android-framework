package com.pixelplex.echoframework.support

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.exception.LocalException

/**
 * Empty instance of operation callback
 *
 * Useful when passing callback is unnecessary
 *
 * @author Dmitriy Bushuev
 */
class EmptyCallback<T> : Callback<T> {

    override fun onSuccess(result: T) {
    }

    override fun onError(error: LocalException) {
    }

}
