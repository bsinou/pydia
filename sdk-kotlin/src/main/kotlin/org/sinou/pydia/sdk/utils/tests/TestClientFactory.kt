package org.sinou.pydia.sdk.utils.tests

import org.sinou.pydia.sdk.api.Server
import org.sinou.pydia.sdk.api.Store
import org.sinou.pydia.sdk.api.Transport
import org.sinou.pydia.sdk.client.CellsClient
import org.sinou.pydia.sdk.client.ClientFactory
import org.sinou.pydia.sdk.transport.CellsTransport
import org.sinou.pydia.sdk.transport.auth.CredentialService

/**
 * This provides a convenient factory that works for everything,
 * except for file transfers TO or FROM a Cells server.
 */
class TestClientFactory(
    credentialService: CredentialService,
    serverStore: Store<Server>,
    transportStore: Store<Transport>
) : ClientFactory(credentialService, serverStore, transportStore) {

    override fun getCellsClient(transport: CellsTransport): CellsClient {
        return CellsClient(transport, NoAwsS3Client())
    }
}