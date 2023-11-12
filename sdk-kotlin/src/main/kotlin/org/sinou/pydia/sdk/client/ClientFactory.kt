package org.sinou.pydia.sdk.client

import org.sinou.pydia.sdk.api.Client
import org.sinou.pydia.sdk.api.Server
import org.sinou.pydia.sdk.api.Store
import org.sinou.pydia.sdk.api.Transport
import org.sinou.pydia.sdk.transport.CellsTransport
import org.sinou.pydia.sdk.transport.ClientData
import org.sinou.pydia.sdk.transport.ClientData.Companion.getInstance
import org.sinou.pydia.sdk.transport.ClientData.Companion.updateInstance
import org.sinou.pydia.sdk.transport.ServerFactory
import org.sinou.pydia.sdk.transport.auth.CredentialService
import org.sinou.pydia.sdk.utils.Log

/**
 * Extends a server factory to manage client concepts.
 */
abstract class ClientFactory(
    credentialService: CredentialService,
    serverStore: Store<Server>,
    transportStore: Store<Transport>
) : ServerFactory(
    credentialService, serverStore, transportStore
) {
    /**
     * Implement this: it is the single entry point to inject the S3 client
     * that is platform specific
     */
    protected abstract fun getCellsClient(transport: CellsTransport?): CellsClient
    fun getClient(transport: Transport?): Client {
        return getCellsClient(transport as CellsTransport?)
    }

    override fun initAppData() {
        val instance = getInstance()

        // Workaround to insure client data are OK:
        // if the AppName has changed, we consider client data are already correctly set.
        if (ClientData.DEFAULT_APP_NAME == instance!!.name) {
            super.initAppData()
            instance.packageID = this.javaClass.getPackage().name
            instance.label = "Cells Java Client"
            instance.name = "CellsJavaClient"
            Log.i(
                "Client factory", "### After Setting client data, App name: "
                        + instance.name + " - " + instance
            )
            updateInstance(instance)
        }
    }
}