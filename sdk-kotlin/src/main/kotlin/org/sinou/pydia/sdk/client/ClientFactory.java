package org.sinou.pydia.sdk.client;

import org.sinou.pydia.sdk.api.Client;
import org.sinou.pydia.sdk.api.Server;
import org.sinou.pydia.sdk.api.Store;
import org.sinou.pydia.sdk.api.Transport;
import org.sinou.pydia.sdk.transport.CellsTransport;
import org.sinou.pydia.sdk.transport.ClientData;
import org.sinou.pydia.sdk.transport.ServerFactory;
import org.sinou.pydia.sdk.transport.auth.CredentialService;
import org.sinou.pydia.sdk.utils.Log;


/**
 * Extends a server factory to manage client concepts.
 */
public abstract class ClientFactory extends ServerFactory {

    public ClientFactory(CredentialService credentialService, Store<Server> serverStore, Store<Transport> transportStore) {
        super(credentialService, serverStore, transportStore);
    }

    /**
     * Implement this: it is the single entry point to inject the S3 client
     * that is platform specific
     */
    protected abstract CellsClient getCellsClient(CellsTransport transport);

    public Client getClient(Transport transport) {
            return getCellsClient((CellsTransport) transport);
    }

    @Override
    protected void initAppData() {
        ClientData instance = ClientData.getInstance();

        // Workaround to insure client data are OK:
        // if the AppName has changed, we consider client data are already correctly set.
        if (ClientData.DEFAULT_APP_NAME.equals(instance.getName())) {
            super.initAppData();
            instance.setPackageID(this.getClass().getPackage().getName());
            instance.setLabel("Cells Java Client");
            instance.setName("CellsJavaClient");
            Log.i("Client factory", "### After Setting client data, App name: "
                    + instance.getName() + " - " + instance);
            ClientData.updateInstance(instance);
        }
    }
}
