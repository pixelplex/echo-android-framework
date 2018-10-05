package org.echo.mobile.framework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import org.echo.mobile.framework.model.*
import java.lang.reflect.Type

/**
 * Class used to encapsulate operations related to the [OperationType.ACCOUNT_CREATE_OPERATION]
 *
 * @author Dmitriy Bushuev
 */
class AccountCreateOperation
/**
 * Account create operation constructor.
 *
 * @param name      User name. Can't be null.
 * @param registrar User id. Can't be null.
 * @param referrer  User id. Can't be null.
 * @param owner     Owner authority to set. Can be null.
 * @param active    Active authority to set. Can be null.
 * @param options   Active authority to set. Can be null.
 * @param fee       The fee to pay. Can be null.
 */
@JvmOverloads constructor(
    val name: String,
    var registrar: Account,
    var referrer: Account,
    val referrerPercent: Int = 0,
    owner: Authority,
    active: Authority,
    options: AccountOptions,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.ACCOUNT_CREATE_OPERATION) {

    private var owner = Optional(owner)
    private var active = Optional(active)
    private var options = Optional(options)

    /**
     * Updates owner value
     * @param owner New owner value
     */
    fun setOwner(owner: Authority) {
        this.owner = Optional(owner)
    }

    /**
     * Updates active value
     * @param active New active value
     */
    fun setActive(active: Authority) {
        this.active = Optional(active)
    }

    /**
     * Updates options value
     * @param options New options value
     */
    fun setAccountOptions(options: AccountOptions) {
        this.options = Optional(options)
    }

    override fun toJsonString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    override fun toJsonObject(): JsonElement {
        val array = JsonArray()
        array.add(this.id)

        val accountUpdate = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(KEY_NAME, name)
            addProperty(KEY_REGISTRAR, registrar.toJsonString())
            addProperty(KEY_REFERRER, referrer.toJsonString())
            addProperty(KEY_REFERRER_PERCENT, referrerPercent)

            if (owner.isSet)
                add(KEY_OWNER, owner.toJsonObject())
            if (active.isSet)
                add(KEY_ACTIVE, active.toJsonObject())
            if (options.isSet)
                add(KEY_OPTIONS, options.toJsonObject())

            add(KEY_EXTENSIONS, extensions.toJsonObject())
        }

        array.add(accountUpdate)
        return array
    }

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val nameBytes = name.toByteArray()
        val registrar = registrar.toBytes()
        val referrer = referrer.toBytes()
        val referrerPercent = byteArrayOf(referrerPercent.toByte())
        val ownerBytes = owner.toBytes()
        val activeBytes = active.toBytes()
        val newOptionsBytes = options.toBytes()
        val extensionBytes = extensions.toBytes()
        return Bytes.concat(
            feeBytes,
            registrar,
            referrer,
            referrerPercent,
            nameBytes,
            ownerBytes,
            activeBytes,
            newOptionsBytes,
            extensionBytes
        )
    }

    /**
     * Deserializer used to build a [AccountCreateOperation] instance from JSON
     */
    class Deserializer : JsonDeserializer<AccountCreateOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): AccountCreateOperation? {
            val jsonObject = json.asJsonObject

            val name = jsonObject.get(KEY_NAME).asString
            val referrer = Account(jsonObject.get(KEY_REFERRER).asString)
            val registrar =Account(jsonObject.get(KEY_REGISTRAR).asString)

            // Deserializing Authority objects
            val owner =
                context.deserialize<Authority>(jsonObject.get(KEY_OWNER), Authority::class.java)
            val active =
                context.deserialize<Authority>(jsonObject.get(KEY_ACTIVE), Authority::class.java)

            //Deserializing AccountOptions object
            val options = context.deserialize<AccountOptions>(
                jsonObject.get(KEY_OPTIONS),
                AccountOptions::class.java
            )

            // Deserializing AssetAmount object
            val fee =
                context.deserialize<AssetAmount>(jsonObject.get(KEY_FEE), AssetAmount::class.java)

            return AccountCreateOperation(name, registrar, referrer, 0, owner, active, options, fee)
        }

    }

    companion object {
        const val KEY_NAME = "name"
        const val KEY_REGISTRAR = "registrar"
        const val KEY_REFERRER = "referrer"
        const val KEY_REFERRER_PERCENT = "referrer_percent"
        const val KEY_OWNER = "owner"
        const val KEY_ACTIVE = "active"
        const val KEY_OPTIONS = "options"
        const val KEY_EXTENSIONS = "extensions"
    }

}