package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.model.Block

/**
 * Retrieve a full, signed block.
 *
 * @param blockNumber Height of the block whose header should be returned
 * @return the referenced [Block] object, or null if no matching block was found
 *
 * @author Daria Pechkovskaya
 */
class GetBlockSocketOperation(
    override val apiId: Int,
    val blockNumber: String,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<Block>

) : SocketOperation<Block>(method, ILLEGAL_ID, Block::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.BLOCK.key)
            add(JsonArray().apply { blockNumber })
        }

    override fun fromJson(json: String): Block? {
        return null
    }
}
