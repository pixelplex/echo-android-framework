package com.pixelplex.echoframework.model.operations

import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.AssetAmount
import com.pixelplex.echoframework.model.BaseOperation
import java.lang.reflect.Type


/**
 * Encapsulates transfer information
 *
 * @author Dmitriy Bushuev
 */
class TransferOperation : BaseOperation {

    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
    var assetAmount: AssetAmount? = null
    var from: Account? = null
    var to: Account? = null

    constructor(
        from: Account,
        to: Account,
        transferAmount: AssetAmount,
        fee: AssetAmount
    ) : super(OperationType.TRANSFER_OPERATION) {
        this.from = from
        this.to = to
        this.assetAmount = transferAmount
        this.fee = fee
    }

    constructor(
        from: Account, to: Account, transferAmount: AssetAmount
    ) : super(
        OperationType.TRANSFER_OPERATION
    ) {
        this.from = from
        this.to = to
        this.assetAmount = transferAmount
    }

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val fromBytes = from!!.toBytes()
        val toBytes = to!!.toBytes()
        val amountBytes = assetAmount!!.toBytes()
        // important to add zero byte in any case!!
        // implement memo
        val memoBytes = 0.toByte()
        val extensions = extensions.toBytes()
        return feeBytes + fromBytes + toBytes + amountBytes + memoBytes + extensions
    }

    override fun toJsonString(): String {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
            TransferOperation::class.java,
            TransferSerializer()
        )
        return gsonBuilder.create().toJson(this)
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(KEY_FROM, from!!.toJsonString())
            addProperty(KEY_TO, to!!.toJsonString())
            add(KEY_AMOUNT, assetAmount!!.toJsonObject())
            add(KEY_EXTENSIONS, extensions.toJsonObject())
        }
        add(jsonObject)
    }

    /**
     * Encapsulates logic of transfer json serialization
     */
    class TransferSerializer : JsonSerializer<TransferOperation> {

        override fun serialize(
            transfer: TransferOperation,
            type: Type,
            jsonSerializationContext: JsonSerializationContext
        ): JsonElement {
            val arrayRep = JsonArray()
            arrayRep.add(transfer.id)
            arrayRep.add(transfer.toJsonObject())
            return arrayRep
        }
    }

    /**
     * This deserializer will work on any transfer operation serialized in the 'array form' used a lot in
     * the Graphene Blockchain API.
     *
     * An example of this serialized form is the following:
     *
     * [
     * 0,
     * {
     * "fee": {
     * "amount": 264174,
     * "asset_id": "1.3.0"
     * },
     * "from": "1.2.138632",
     * "to": "1.2.129848",
     * "amount": {
     * "amount": 100,
     * "asset_id": "1.3.0"
     * },
     * "extensions": []
     * }
     * ]
     *
     * It will convert this data into a nice TransferOperation object.
     */
    class TransferDeserializer : JsonDeserializer<TransferOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): TransferOperation? {

            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val amount = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_AMOUNT),
                AssetAmount::class.java
            )
            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val from = Account(jsonObject.get(KEY_FROM).asString)
            val to = Account(jsonObject.get(KEY_TO).asString)

            return TransferOperation(
                from,
                to,
                amount,
                fee
            )
        }
    }

    companion object {
        private const val KEY_AMOUNT = "amount"
        private const val KEY_FROM = "from"
        private const val KEY_TO = "to"
        private const val KEY_FEE = "fee"
        private const val KEY_EXTENSIONS = "extensions"
    }
}