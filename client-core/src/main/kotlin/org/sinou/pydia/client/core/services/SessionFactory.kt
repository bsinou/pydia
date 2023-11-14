package org.sinou.pydia.client.core.services

import android.util.Log
import org.sinou.pydia.client.core.db.accounts.AccountDB
import org.sinou.pydia.client.core.db.accounts.RSessionView
import org.sinou.pydia.client.core.transfer.CellsS3Client
import org.sinou.pydia.sdk.api.Client
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.Server
import org.sinou.pydia.sdk.api.Store
import org.sinou.pydia.sdk.api.Transport
import org.sinou.pydia.sdk.client.CellsClient
import org.sinou.pydia.sdk.client.ClientFactory
import org.sinou.pydia.sdk.transport.CellsTransport
import org.sinou.pydia.sdk.transport.ServerURLImpl
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.utils.Str

/**
 * The android specific session factory wraps the Java SDK Server and Client Factories,
 * to provide a Android specific S3 client for file transfer with cells.
 * It also holds server and transport memory stores that are used by the SDK to manage clients.
 */
class SessionFactory(
    private val networkService: NetworkService,
    credentialService: AppCredentialService,
    serverStore: Store<Server>,
    private val transportStore: Store<Transport>,
    accountDB: AccountDB
) : ClientFactory(credentialService, serverStore, transportStore) {

    private val logTag = "SessionFactory"

    private val sessionViewDao = accountDB.sessionViewDao()
    private val accountDao = accountDB.accountDao()

    override fun getServer(id: String): Server? {
        val stateID = StateID.safeFromId(id)
        super.getServer(id)?.let { return it }

        // If not yet defined, try to register it
        val sessionView = sessionViewDao.getSession(stateID.accountId)
        if (sessionView != null) {
            val currTransport =
                transportStore.get(stateID.accountId) ?: prepareTransport(sessionView)
            return currTransport.server
        }

        if (Str.empty(stateID.username)) {
            // We check if an account with same URL is registered and get the server
            val accounts = accountDao.getAccountByUrl(stateID.serverUrl)
            if (accounts.isNotEmpty()) {
                val serverURL = ServerURLImpl.fromAddress(accounts[0].url, accounts[0].skipVerify())
                return registerServer(serverURL)
            }
        }
        throw SDKException(ErrorCodes.not_found, "cannot retrieve server for $stateID")
    }

    /**
     * Enable Callback from ancestor classes in the JAVA SDK.
     */
    override fun getCellsClient(transport: CellsTransport): CellsClient {

        // We also check if we need a token refresh at this point
        // TODO double check if we still need this, normally this should be done by the connection service
        // transport.requestTokenRefresh()

        return CellsClient(
            transport,
            CellsS3Client(transport)
        )
    }

    fun getTransport(stateID: StateID): Transport? {
        return transportStore.get(stateID.accountId)
    }

    @Throws(SDKException::class)
    private fun internalGetClient(accountID: StateID): Client {

        // At this point we are quite sure we have a connection to the internet...
        // Yet we still code defensively afterwards and correctly handle errors
        val sessionView: RSessionView = sessionViewDao.getSession(accountID.accountId)
            ?: run {
                throw SDKException(ErrorCodes.not_found, "cannot retrieve client for $accountID")
            }

        if (sessionView.isLoggedIn()) {
            var currTransport = transportStore.get(accountID.id)
            if (currTransport == null) {
                currTransport = prepareTransport(sessionView)
            }
            return getClient(currTransport)
        } else {
            Log.d(logTag, "... Required session is not connected, listing known sessions:")
            for (currentSession in sessionViewDao.getSessions()) {
                Log.d(logTag, "$currentSession.dbName} / ${currentSession.authStatus}")
            }
            throw SDKException(
                ErrorCodes.authentication_required,
                "cannot unlock session for $accountID, auth status: " + sessionView.authStatus
            )
        }
    }

    @Throws(SDKException::class)
    suspend fun getUnlockedClient(accountID: StateID): Client {
        if (!networkService.isConnected())
            throw SDKException(ErrorCodes.no_internet, "No internet connection is available")

        return internalGetClient(accountID)
    }

    private fun prepareTransport(sessionView: RSessionView): Transport {
//        try {
        val skipVerify = sessionView.tlsMode == 1
        val serverURL = ServerURLImpl.fromAddress(sessionView.url, skipVerify)
        return restoreAccount(serverURL, sessionView.username)
//        } catch (se: SDKException) {
//            throw se
//        }
    }
}
