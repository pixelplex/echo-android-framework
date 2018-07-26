package com.pixelplex.echoframework.facade

import com.pixelplex.echoframework.Callback

/**
 * Encapsulates logic, associated with fee configuration processes
 *
 * @author Dmitriy Bushuev
 */
interface FeeFacade {

    /**
     * Counts required fee for defined transaction settings
     *
     * @param fromNameOrId  Source account name or id
     * @param toNameOrId    Target account name or id
     * @param amount        Amount value of transfer
     * @param asset         Specific asset type id
     * @param callback      Listener of operation results
     */
    fun getFeeForTransferOperation(
        fromNameOrId: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        callback: Callback<String>
    )

}
