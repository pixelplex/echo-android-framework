package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.AssetOptions
import org.echo.mobile.framework.model.HistoricalTransfer
import org.echo.mobile.framework.model.HistoryResponse
import org.echo.mobile.framework.model.Memo
import org.echo.mobile.framework.model.eddsa.EdAuthority
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.operations.AccountCreateOperation
import org.echo.mobile.framework.model.operations.AccountUpdateOperation
import org.echo.mobile.framework.model.operations.ContractCallOperation
import org.echo.mobile.framework.model.operations.ContractCreateOperation
import org.echo.mobile.framework.model.operations.ContractTransferOperation
import org.echo.mobile.framework.model.operations.CreateAssetOperation
import org.echo.mobile.framework.model.operations.GenerateEthereumAddressOperation
import org.echo.mobile.framework.model.operations.IssueAssetOperation
import org.echo.mobile.framework.model.operations.SidechainBurnSocketOperation
import org.echo.mobile.framework.model.operations.SidechainIssueSocketOperation
import org.echo.mobile.framework.model.operations.TransferOperation
import org.echo.mobile.framework.model.operations.WithdrawEthereumOperation

/**
 * Get operations relevant to the specified account.
 *
 * @param accountId The account whose history should be queried
 * @param stopId Id of the earliest operation to retrieve
 * @param limit Maximum number of operations to retrieve (must not exceed 100)
 * @param startId Id of the most recent operation to retrieve
 *
 * @return A list of [HistoricalTransfer] objects performed by account, ordered from most recent
 * to oldest.
 *
 * @author Daria Pechkovskaya
 */
class GetAccountHistorySocketOperation(
    override val apiId: Int,
    val accountId: String,
    val startId: String = DEFAULT_HISTORY_ID,
    val stopId: String = DEFAULT_HISTORY_ID,
    val limit: Int = DEFAULT_LIMIT,
    val network: Network,
    callId: Int,
    callback: Callback<HistoryResponse>

) : SocketOperation<HistoryResponse>(
    SocketMethodType.CALL,
    callId,
    HistoryResponse::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.ACCOUNT_HISTORY.key)
            add(JsonArray().apply {
                add(accountId)
                add(startId)
                add(limit)
                add(stopId)
            })
        }

    override fun fromJson(json: String): HistoryResponse? {
        val gson = configureGson()

        val responseType = object : TypeToken<HistoryResponse>() {
        }.type

        return gson.fromJson<HistoryResponse>(json, responseType)
    }

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(
            HistoricalTransfer::class.java,
            HistoricalTransfer.HistoryDeserializer()
        )
        registerTypeAdapter(
            AccountUpdateOperation::class.java,
            AccountUpdateOperation.Deserializer()
        )
        registerTypeAdapter(
            AccountCreateOperation::class.java,
            AccountCreateOperation.Deserializer()
        )
        registerTypeAdapter(
            TransferOperation::class.java,
            TransferOperation.TransferDeserializer()
        )
        registerTypeAdapter(
            ContractCreateOperation::class.java,
            ContractCreateOperation.Deserializer()
        )
        registerTypeAdapter(
            ContractCallOperation::class.java,
            ContractCallOperation.Deserializer()
        )
        registerTypeAdapter(
            ContractTransferOperation::class.java,
            ContractTransferOperation.Deserializer()
        )
        registerTypeAdapter(
            CreateAssetOperation::class.java,
            CreateAssetOperation.CreateAssetDeserializer()
        )
        registerTypeAdapter(
            AssetOptions::class.java,
            AssetOptions.AssetOptionsDeserializer()
        )
        registerTypeAdapter(
            Memo::class.java,
            Memo.MemoDeserializer(network)
        )
        registerTypeAdapter(
            IssueAssetOperation::class.java,
            IssueAssetOperation.IssueAssetDeserializer()
        )
        registerTypeAdapter(
            GenerateEthereumAddressOperation::class.java,
            GenerateEthereumAddressOperation.GenerateEthereumAddressDeserializer()
        )
        registerTypeAdapter(
            WithdrawEthereumOperation::class.java,
            WithdrawEthereumOperation.WithdrawEthereumOperationDeserializer()
        )
        registerTypeAdapter(
            SidechainIssueSocketOperation::class.java,
            SidechainIssueSocketOperation.SidechainIssueDeserializer()
        )
        registerTypeAdapter(
            SidechainBurnSocketOperation::class.java,
            SidechainBurnSocketOperation.SidechainBurnDeserializer()
        )
        registerTypeAdapter(AssetAmount::class.java, AssetAmount.Deserializer())
        registerTypeAdapter(EdAuthority::class.java, EdAuthority.Deserializer())
        registerTypeAdapter(Account::class.java, Account.Deserializer())
        registerTypeAdapter(AccountOptions::class.java, AccountOptions.Deserializer(network))
    }.create()

    companion object {
        const val DEFAULT_HISTORY_ID = "1.10.0"
        const val DEFAULT_LIMIT = 100
    }

}
