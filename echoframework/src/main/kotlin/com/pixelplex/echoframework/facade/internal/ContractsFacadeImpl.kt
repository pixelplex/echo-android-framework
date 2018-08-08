package com.pixelplex.echoframework.facade.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.facade.ContractsFacade
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.AuthorityType
import com.pixelplex.echoframework.model.Transaction
import com.pixelplex.echoframework.model.contract.*
import com.pixelplex.echoframework.model.operations.ContractOperationBuilder
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.service.NetworkBroadcastApiService
import com.pixelplex.echoframework.support.Result
import com.pixelplex.echoframework.support.dematerialize
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.value

/**
 * Implementation of [ContractsFacade]
 *
 * Delegates API call logic to [DatabaseApiService] and [NetworkBroadcastApiService]
 *
 * @author Daria Pechkovskaya
 */
class ContractsFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent), ContractsFacade {

    override fun createContract(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        byteCode: String,
        callback: Callback<Boolean>
    ) {
        Result {
            var registrar: Account? = null

            databaseApiService.getFullAccounts(listOf(registrarNameOrId), false)
                .value { accountsMap ->
                    registrar = accountsMap[registrarNameOrId]?.account
                            ?:
                            throw LocalException("Unable to find required account $registrarNameOrId")
                }
                .error { accountsError ->
                    throw LocalException("Error occurred during accounts request", accountsError)
                }

            checkOwnerAccount(registrarNameOrId, password, registrar!!)

            val privateKey = cryptoCoreComponent.getPrivateKey(
                registrar!!.name,
                password,
                AuthorityType.ACTIVE
            )

            val contractOperation = ContractOperationBuilder()
                .setAsset(assetId)
                .setRegistrar(registrar!!)
                .setGas(1000000)
                .setContractCode(byteCode)
                .build()

            val blockData = databaseApiService.getBlockData()
            val chainId = getChainId()
            val fees = getFees(listOf(contractOperation), assetId)

            val transaction = Transaction(privateKey, blockData, listOf(contractOperation), chainId)
                .apply { setFees(fees) }

            networkBroadcastApiService.broadcastTransactionWithCallback(transaction).dematerialize()
        }
            .value { creationResult -> callback.onSuccess(creationResult) }
            .error { error -> callback.onError(LocalException(error)) }

    }

    override fun callContract(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<ContractMethodParameter>,
        callback: Callback<Boolean>
    ) {
        Result {
            var registrar: Account? = null

            databaseApiService.getFullAccounts(listOf(registrarNameOrId), false)
                .value { accountsMap ->
                    registrar = accountsMap[registrarNameOrId]?.account
                            ?:
                            throw LocalException("Unable to find required account $registrarNameOrId")
                }
                .error { accountsError ->
                    throw LocalException("Error occurred during accounts request", accountsError)
                }

            checkOwnerAccount(registrarNameOrId, password, registrar!!)

            val privateKey = cryptoCoreComponent.getPrivateKey(
                registrar!!.name,
                password,
                AuthorityType.ACTIVE
            )

            val contractCode = ContractCodeBuilder()
                .setMethodName(methodName)
                .setMethodParams(methodParams)
                .build()

            val contractOperation = ContractOperationBuilder()
                .setAsset(assetId)
                .setRegistrar(registrar!!)
                .setGas(1000000)
                .setReceiver(contractId)
                .setContractCode(contractCode)
                .build()

            val blockData = databaseApiService.getBlockData()
            val chainId = getChainId()
            val fees = getFees(listOf(contractOperation), assetId)

            val transaction = Transaction(privateKey, blockData, listOf(contractOperation), chainId)
                .apply { setFees(fees) }

            networkBroadcastApiService.broadcastTransactionWithCallback(transaction).dematerialize()
        }
            .value { callContractResult -> callback.onSuccess(callContractResult) }
            .error { error -> callback.onError(LocalException(error)) }

    }

    override fun queryContract(
        registrarNameOrId: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<ContractMethodParameter>,
        callback: Callback<String>
    ) {
        Result {
            var registrar: Account? = null

            databaseApiService.getFullAccounts(listOf(registrarNameOrId), false)
                .value { accountsMap ->
                    registrar = accountsMap[registrarNameOrId]?.account
                            ?:
                            throw LocalException("Unable to find required account $registrarNameOrId")
                }
                .error { accountsError ->
                    throw LocalException("Error occurred during accounts request", accountsError)
                }

            val contractCode = ContractCodeBuilder()
                .setMethodName(methodName)
                .setMethodParams(methodParams)
                .build()

            databaseApiService.callContractNoChangingState(
                contractId,
                registrar!!.getObjectId(),
                assetId,
                contractCode
            ).dematerialize()
        }
            .value { callContractResult -> callback.onSuccess(callContractResult) }
            .error { error -> callback.onError(LocalException(error)) }
    }

    override fun getContractResult(historyId: String, callback: Callback<ContractResult>) {
        Result { databaseApiService.getContractResult(historyId).dematerialize() }
            .value { callContractResult -> callback.onSuccess(callContractResult) }
            .error { error -> callback.onError(LocalException(error)) }
    }

    override fun getContracts(contractIds: List<String>, callback: Callback<List<ContractInfo>>) {
        Result { databaseApiService.getContracts(contractIds).dematerialize() }
            .value { callContractResult -> callback.onSuccess(callContractResult) }
            .error { error -> callback.onError(LocalException(error)) }
    }

    override fun getAllContracts(callback: Callback<List<ContractInfo>>) {
        Result { databaseApiService.getAllContracts().dematerialize() }
            .value { callContractResult -> callback.onSuccess(callContractResult) }
            .error { error -> callback.onError(LocalException(error)) }
    }

    override fun getContract(contractId: String, callback: Callback<ContractStruct>) {
        Result { databaseApiService.getContract(contractId).dematerialize() }
            .value { callContractResult -> callback.onSuccess(callContractResult) }
            .error { error -> callback.onError(LocalException(error)) }
    }
}
