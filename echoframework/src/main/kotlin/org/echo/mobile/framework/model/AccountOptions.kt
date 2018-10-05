package org.echo.mobile.framework.model

import com.google.common.primitives.Bytes
import com.google.gson.*
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.support.Uint16
import org.echo.mobile.framework.support.serialize
import org.echo.mobile.framework.exception.MalformedAddressException
import org.echo.mobile.framework.model.network.Network
import java.lang.reflect.Type

/**
 * Contains user account additional information
 *
 * These are the fields which can be updated by the active authority
 *
 * [Account options details][https://dev-doc.myecho.app/structgraphene_1_1chain_1_1account__options.html]
 *
 * @author Dmitriy Bushuev
 */
class AccountOptions : GrapheneSerializable {

    var memoKey: PublicKey? = null

    var votingAccount: Account = Account(Account.PROXY_TO_SELF)

    var witnessCount: Int = 0

    var committeeCount: Int = 0

    var votes: Array<String> = arrayOf()

    private val extensions = Extensions()

    constructor()

    constructor(memoKey: PublicKey) {
        this.memoKey = memoKey
    }

    override fun toBytes(): ByteArray =
        memoKey?.let { memo ->
            // Adding memo key
            val memoBytes = memo.toBytes()

            // Adding voting account
            val voitingAccountBytes = votingAccount.toBytes()

            // Adding num_witness
            val witnessCountBytes = Uint16.serialize(witnessCount)

            // Adding num_committee
            val committeeCountBytes = Uint16.serialize(committeeCount)

            // Vote's array length
            val votesBytes = votes.serialize { vote -> vote.toByteArray() }

            // Account options's extensions
            val extensionsBytes = extensions.toBytes()

            Bytes.concat(
                memoBytes, voitingAccountBytes, witnessCountBytes, committeeCountBytes,
                votesBytes, extensionsBytes
            )

        } ?: byteArrayOf(0)

    override fun toJsonString(): String? = null

    override fun toJsonObject(): JsonElement? =
        JsonObject().apply {
            addProperty(KEY_MEMO_KEY, Address(memoKey!!).toString())
            addProperty(KEY_NUM_COMMITTEE, committeeCount)
            addProperty(KEY_NUM_WITNESS, witnessCount)
            addProperty(KEY_VOTING_ACCOUNT, votingAccount.getObjectId())

            val votesArray = JsonArray().apply {
                votes.forEach { vote -> add(vote) }
            }

            add(KEY_VOTES, votesArray)
            add(KEY_EXTENSIONS, extensions.toJsonObject())
        }

    /**
     * Deserializer used to build a [AccountOptions] instance from the full JSON-formatted response
     * obtained by the API call.
     */
    class Deserializer(val network: Network) : JsonDeserializer<AccountOptions> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): AccountOptions? {

            if (json == null || !json.isJsonObject) {
                return null
            }

            val jsonAccountOptions = json.asJsonObject

            return try {
                val memoKeyString = jsonAccountOptions.get(KEY_MEMO_KEY).asString
                val address = Address(memoKeyString, network)
                AccountOptions(address.pubKey)
            } catch (e: MalformedAddressException) {
                LOGGER.log("Invalid address deserialization", e)
                AccountOptions()
            }.apply {
                votingAccount = Account(jsonAccountOptions.get(KEY_VOTING_ACCOUNT).asString)
                witnessCount = jsonAccountOptions.get(KEY_NUM_WITNESS).asInt
                committeeCount = jsonAccountOptions.get(KEY_NUM_COMMITTEE).asInt
            }
        }
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(AccountOptions::class.java.name)

        const val KEY_MEMO_KEY = "memo_key"
        const val KEY_NUM_COMMITTEE = "num_committee"
        const val KEY_NUM_WITNESS = "num_witness"
        const val KEY_VOTES = "votes"
        const val KEY_VOTING_ACCOUNT = "voting_account"
        const val KEY_EXTENSIONS = Extensions.KEY_EXTENSIONS
    }

}