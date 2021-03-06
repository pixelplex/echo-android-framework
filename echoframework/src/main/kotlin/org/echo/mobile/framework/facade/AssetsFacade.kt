package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Asset

/**
 * Encapsulates logic, associated with echo blockchain assets use cases
 *
 * @author Dmitriy Bushuev
 */
interface AssetsFacade {

    /**
     * Creates [asset] with required parameters.
     *
     * @param broadcastCallback Callback for result of operation broadcast
     * @param resultCallback Callback for retrieving result of operation  (not required)
     */
    fun createAsset(
        name: String,
        password: String,
        asset: Asset,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>? = null
    )

    /**
     * Creates [asset] with required parameters using [name] account's [wif] for transaction signing
     *
     * @param broadcastCallback Callback for result of operation broadcast
     * @param resultCallback Callback for retrieving result of operation  (not required)
     */
    fun createAssetWithWif(
        name: String,
        wif: String,
        asset: Asset,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>? = null
    )

    /**
     * Issues [asset] from [issuerNameOrId] account to [destinationIdOrName] account using source
     * account [password] for signature
     */
    fun issueAsset(
        issuerNameOrId: String,
        password: String,
        asset: String,
        amount: String,
        destinationIdOrName: String,
        message: String?,
        callback: Callback<Boolean>
    )

    /**
     * Issues [asset] from [issuerNameOrId] account to [destinationIdOrName] account using source
     * account [wif] for signature
     */
    fun issueAssetWithWif(
        issuerNameOrId: String,
        wif: String,
        asset: String,
        amount: String,
        destinationIdOrName: String,
        message: String?,
        callback: Callback<Boolean>
    )

    /**
     * Query list of assets by required asset symbol [lowerBound] with limit [limit]
     */
    fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>)

    /**
     * Query list of assets by it's ids [assetIds]
     */
    fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>)

    /**
     * Query list of assets by it's [symbolsOrIds]
     */
    fun lookupAssetsSymbols(symbolsOrIds: List<String>, callback: Callback<List<Asset>>)

}
