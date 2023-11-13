package org.sinou.pydia.sdk.utils.tests

import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.Store
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.transport.auth.CredentialService
import org.sinou.pydia.sdk.transport.auth.Token

class TestCredentialService(tokenStore: Store<Token>, passwordStore: Store<String>) :
    CredentialService(tokenStore, passwordStore) {

    @Throws(SDKException::class)
    override fun requestRefreshToken(stateID: StateID) {

//        Token oldToken = get(id);
//        if (oldToken == null) {
//            throw new SDKException(
//                    ErrorCodes.no_token_available,
//                    "Cannot refresh unknown token for " + id
//            );
//        }
//        if (transport instanceof CellsTransport)
//            return; // TODO
//            // return ((CellsTransport) transport).getRefreshedOAuthToken(oldToken.refreshToken);
//        else {
//            throw new SDKException(ErrorCodes.internal_error, "Refresh OAuth token is not supported by P8");
//        }
    }
}