package org.sinou.pydia.client.core.transfer

import android.util.Log
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.transport.CellsTransport
import org.sinou.pydia.sdk.transport.StateID

class CellsAuthProvider(
    private val transport: CellsTransport,
    private val accountID: StateID
) : AWSCredentialsProvider {

    private val DEFAULT_GATEWAY_SECRET = "gatewaysecret"
    private val logTag = "CellsAuthProvider"

    override fun getCredentials(): AWSCredentials {
        return BasicAWSCredentials(transport.accessToken, DEFAULT_GATEWAY_SECRET)
    }

    override fun refresh() {
        Log.i(logTag, "Explicit token refresh request for $accountID")
        try {
            transport.requestTokenRefresh()
        } catch (se: SDKException) {
            Log.e(logTag, "Unexpected error while requesting token refresh for $accountID")
            Log.e(logTag, "#${se.code}: ${se.message}")
            se.printStackTrace()
        }
    }
}
