package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.DEFAULT_GAS_LIMIT
import org.echo.mobile.framework.DEFAULT_GAS_PRICE
import org.echo.mobile.framework.model.Log
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.input.InputValue
import java.math.BigInteger

/**
 * Encapsulates logic, associated with various blockchain smart contract processes
 *
 * @author Daria Pechkovskaya
 */
interface ContractsFacade {

    /**
     * Creates contract on blockchain
     *
     * @param registrarNameOrId     Name or id of account that creates the contract
     * @param password              Password from account for transaction signature
     * @param assetId               Asset of contract
     * @param feeAsset              Asset for fee pay
     * @param byteCode              Bytecode of the created contract
     * @param params                Params for contract constructor
     * @param gasLimit              Gas limit for contract operation
     * @param gasPrice              One gas price for contract operation
     *
     * @param broadcastCallback     Callback for result of operation broadcast
     * @param resultCallback        Callback for retrieving result of operation (not required).
     *                              Retrieves result of transactions if exists -
     *                              history id which contains call contract result,
     *                              if not exists - empty string
     */
    fun createContract(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        feeAsset: String?,
        byteCode: String,
        params: List<InputValue> = listOf(),
        gasLimit: Long = DEFAULT_GAS_LIMIT,
        gasPrice: Long = DEFAULT_GAS_PRICE,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>? = null
    )

    /**
     * Calls to contract on blockchain
     *
     * @param userNameOrId          Name or id of account that calls the contract
     * @param password              Password from account for transaction signature
     * @param assetId               Asset of contract
     * @param feeAsset              Asset for fee pay
     * @param contractId            Id of called contract
     * @param methodName            Name of called method
     * @param methodParams          Parameters of calling method
     * @param value                 Amount for payable methods
     * @param gasLimit              Gas limit for contract operation
     * @param gasPrice              One gas price for contract operation
     * @param broadcastCallback     Callback for result of operation deploying
     * @param resultCallback        Callback for retrieving result of operation (not required).
     *                              Retrieves result of transactions if exists -
     *                              history id which contains call contract result,
     *                              if not exists - empty string
     */
    fun callContract(
        userNameOrId: String,
        password: String,
        assetId: String,
        feeAsset: String?,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        value: String = BigInteger.ZERO.toString(),
        gasLimit: Long = DEFAULT_GAS_LIMIT,
        gasPrice: Long = DEFAULT_GAS_PRICE,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>? = null
    )

    /**
     * Calls contract method without changing state of blockchain
     *
     * @param userNameOrId Name or id of account that calls the contract
     * @param contractId   Id of called contract
     * @param assetId      Asset of contract
     * @param methodName   Name of calling method
     * @param methodParams Parameters of called method
     * @param callback     Listener of operation results.
     */
    fun queryContract(
        userNameOrId: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        callback: Callback<String>
    )

    /**
     * Return result of contract operation call
     *
     * @param historyId History operation id
     */
    fun getContractResult(historyId: String, callback: Callback<ContractResult>)

    /**
     * Return list of contract logs
     *
     * @param contractId   Contract id for fetching logs
     * @param fromBlock    Number of the earliest block to retrieve
     * @param toBlock      Number of the most recent block to retrieve
     * @param callback     Listener of operation results.
     */
    fun getContractLogs(
        contractId: String,
        fromBlock: String,
        toBlock: String,
        callback: Callback<List<Log>>
    )

    /**
     * Returns contracts called by ids
     *
     * @param contractIds List of contracts ids
     */
    fun getContracts(contractIds: List<String>, callback: Callback<List<ContractInfo>>)

    /**
     * Returns all existing contracts from blockchain
     */
    fun getAllContracts(callback: Callback<List<ContractInfo>>)

    /**
     * Return contract code by [contractId]
     *
     * @param contractId Id of contract
     */
    fun getContract(contractId: String, callback: Callback<String>)

}
