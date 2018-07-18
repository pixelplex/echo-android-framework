package com.pixelplex.echolib.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.AssetAmount
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.getId

/**
 * This function fetches all relevant [Account] objects for the given accounts, and
 * subscribes to updates to the given accounts. If any of the strings in [namesOrIds] cannot be
 * tied to an account, that input will be ignored. All other accounts will be retrieved and
 * subscribed.
 *
 * @param nameOrId Object if of required account!
 * @param asset Required asset type
 * @param shouldSubscribe Flag of subscription on updates
 *
 * @author Daria Pechkovskaya
 */
class AccountBalancesOperation(
    val api: Api,
    val nameOrId: String,
    val asset: String,
    val shouldSubscribe: Boolean,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<AssetAmount>
) : SocketOperation<AssetAmount>(
    method,
    ILLEGAL_ID,
    AssetAmount::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.ACCOUNT_BALANCES.key)

            val dataJson = JsonArray()

            dataJson.add(nameOrId)

            val assetsJson = JsonArray()

            assetsJson.add(asset)

            dataJson.add(assetsJson)
            dataJson.add(shouldSubscribe)

            add(JsonArray().apply { addAll(dataJson) })
        }

    override val apiId: Int
        get() = api.getId()

    override fun fromJson(json: String): AssetAmount? {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
            AssetAmount::class.java,
            AssetAmount.AssetAmountDeserializer()
        )

        val responseType = object : TypeToken<AssetAmount>() {
        }.type

        return gsonBuilder.create()
            .fromJson<AssetAmount>(json, responseType)
    }

}
