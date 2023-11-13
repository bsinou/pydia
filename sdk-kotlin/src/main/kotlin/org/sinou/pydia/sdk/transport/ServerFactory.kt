package org.sinou.pydia.sdk.transport

import org.sinou.pydia.sdk.api.Credentials
import org.sinou.pydia.sdk.api.CustomEncoder
import org.sinou.pydia.sdk.api.IServerFactory
import org.sinou.pydia.sdk.api.PasswordCredentials
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.Server
import org.sinou.pydia.sdk.api.ServerURL
import org.sinou.pydia.sdk.api.Store
import org.sinou.pydia.sdk.api.Transport
import org.sinou.pydia.sdk.transport.ClientData.Companion.getInstance
import org.sinou.pydia.sdk.transport.ClientData.Companion.updateInstance
import org.sinou.pydia.sdk.transport.auth.CredentialService
import org.sinou.pydia.sdk.transport.auth.credentials.JWTCredentials
import org.sinou.pydia.sdk.utils.KotlinCustomEncoder
import org.sinou.pydia.sdk.utils.Log

/**
 * Optimistic implementation of a IServerFactory that relies on provided stores to persist objects.
 */
open class ServerFactory(
    private val credentialService: CredentialService,
    private val serverStore: Store<Server>,
    private val transportStore: Store<Transport>
) : IServerFactory {

    init {
        initAppData()
    }

    private val logTag = "ServerFactory"

// FIXME was
//     protected open fun initAppData() {
        fun initAppData() {

        val instance = getInstance()
        if (ClientData.DEFAULT_APP_NAME == instance.name) {
            instance.packageID = this.javaClass.getPackage().name
            instance.name = "CellsJavaTransport"
            instance.label = "Cells Java Transport"
            // TODO this should not be hard coded
            instance.version = "0.4.4"
            instance.platform = "Java"
            updateInstance(instance)
        }
    }

//    @Throws(SDKException::class)
//    override fun checkServer(serverURL: ServerURL): String {
//
//        // Insure server is up and SSL is valid
//        try {
//            serverURL.ping()
//        } catch (se: SSLException) {
//            throw SDKException(
//                ErrorCodes.ssl_error,
//                "Un-valid TLS connection with " + serverURL.id,
//                se
//            )
//        } catch (ce: IOException) {
//            throw SDKException(
//                ErrorCodes.unreachable_host,
//                "Cannot reach server at " + serverURL.id,
//                ce
//            )
//        }
//
//        // We do not have any other choice than to try the various well-known endpoints
//        return try {
//            val currURL = serverURL.withPath(CellsServer.BOOTCONF_PATH)
//            currURL!!.ping()
//            SdkNames.TYPE_CELLS
//        } catch (ce: IOException) {
//            throw SDKException(ErrorCodes.not_found, serverURL.id, ce)
//        } catch (e: SDKException) {
//            if (e.code == 404) {
//                throw SDKException(ErrorCodes.not_pydio_server, serverURL.id, e)
//            }
//            throw e
//        }
//    }

    @Throws(SDKException::class)
    override fun registerServer(serverURL: ServerURL): Server {
        serverStore[serverURL.id]?.let {
            return it
        }
        val server = CellsServer(serverURL).refresh(false)
        serverStore.put(serverURL.id, server)
        return server
    }

    protected val servers: Map<String, Server>
        get() = serverStore.getAll()

    /**
     * Retrieve an already registered server by id.
     *
     * @param id can be a server URL or an encoded account id (including the username)
     */
    fun getServer(id: String): Server? {
        val stateID = StateID.fromId(id)
        return serverStore[stateID!!.accountId]
    }

    protected fun removeServer(id: String) {
        serverStore.remove(id)
    }

    // TODO Add a boolean skipCredentialValidation flag?
    @Throws(SDKException::class)
    override fun registerAccountCredentials(
        serverURL: ServerURL,
        credentials: Credentials
    ): String {
        val accountID = accountID(credentials.getUsername(), serverURL)
        var server = serverStore[serverURL.id]
        if (server == null) {
            server = registerServer(serverURL)
        }
        val transport: Transport =
            CellsTransport(credentialService, credentials.getUsername(), server, getEncoder())

        if (credentials is PasswordCredentials) {
            // In the case of legacy password credentials,
            // we rely on the transport to retrieve the effective secure token.
            // TODO might be a better place to forget about the precedent token
            //  that have been generated by P8 but this is compulsory at this time,
            //  among others, to force regeneration of the cookies.
            credentialService.remove(accountID)

            // Retrieve token and save both the credentials (for later refresh) and the newly created token.
            credentialService.put(accountID, transport.getTokenFromLegacyCredentials(credentials))
            credentialService.putPassword(accountID, credentials.getPassword())
        } else if (credentials is JWTCredentials) {
            // In case of JWT credentials, we directly store the token in the token store
            // But we do not try to login at this time
            credentialService.put(accountID, credentials.token)
        }

        // TODO make a call to the server to insure everything is correctly set ?
        transportStore.put(accountID(credentials.getUsername(), server), transport)
        return accountID
    }

    /* Relies on the CredentialService to resurrect an account if we have valid credentials. */
    @Throws(SDKException::class)
    fun restoreAccount(serverURL: ServerURL, username: String): Transport {
        val accountID = accountID(username, serverURL)
        val existing = transportStore[accountID]
        if (existing != null) {
            return existing
        }

        // We must register server even if we have no valid credentials, in order to be able
        // to trigger re-log action.
        val server = registerServer(serverURL)
        val token = credentialService[accountID]
        val pwd = credentialService.getPassword(accountID)
        if (token == null && pwd == null) {
            Log.w(logTag, "## Restoring account $accountID with no valid credentials")
        }
        val transport: Transport =
            CellsTransport(
                credentialService,
                username,
                server,
                getEncoder()
            )
        transportStore.put(accountID, transport)
        return transport
    }

    @Throws(SDKException::class)
    override fun unregisterAccount(accountID: String) {
        transportStore.remove(accountID)
        credentialService.remove(accountID)
        credentialService.removePassword(accountID)
        // When no transport is defined because we do not have credentials, we still keep the server
        // to be able to re-log. // TODO("rather rely on a dynamic cache for the server store")
    }

    override fun getTransport(accountId: String): Transport? {
        return transportStore[accountId]
    }

    override fun getEncoder(): CustomEncoder {
        return KotlinCustomEncoder()
    }

    fun getAnonymousTransport(serverID: String): Transport? {
        val server = serverStore[serverID] ?: return null
        return CellsTransport.asAnonymous(server, getEncoder())
    }

    companion object {
        private const val logTag = "ServerFactory"

        // Static helpers to ease implementation
        fun accountID(username: String?, urlStr: String?): String {
            return StateID(username, urlStr!!).id
        }

        fun accountID(username: String?, serverURL: ServerURL): String {
            return accountID(username, serverURL.id)
        }

        @JvmStatic
        fun accountID(username: String?, server: Server?): String {
            return accountID(username, server!!.serverURL)
        }
        /**
         * Convenience method to create a server factory that only relies on memory stores for quick testing
         */
        //    public ServerFactory() {
        //        this(new CredentialService(new MemoryStore<>(), new MemoryStore<>()),
        //                new MemoryStore<>(), new MemoryStore<>());
        //    }
        //    public void unregisterAccount(ServerURL serverURL, String login) throws SDKException {
        //        unregisterAccount(accountID(login, serverURL));
        //    }
    }
}