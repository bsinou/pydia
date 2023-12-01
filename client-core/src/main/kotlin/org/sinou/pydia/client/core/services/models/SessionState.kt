package org.sinou.pydia.client.core.services.models

import org.sinou.pydia.client.core.LoginStatus
import org.sinou.pydia.client.core.NetworkStatus
import org.sinou.pydia.client.core.db.accounts.RSessionView
import org.sinou.pydia.sdk.transport.StateID

data class SessionState(
    val accountID: StateID,
    val isServerReachable: Boolean,
    val networkStatus: NetworkStatus,
    val loginStatus: LoginStatus,
    val isServerLegacy: Boolean = false
) {
    companion object {
        fun from(view: RSessionView, status: NetworkStatus): SessionState {
            return SessionState(
                accountID = view.getStateID(),
                isServerReachable = view.isReachable,
                networkStatus = status,
                loginStatus = LoginStatus.fromId(view.authStatus),
            )
        }

        val NONE: SessionState = SessionState(
            accountID = StateID.NONE,
            networkStatus = NetworkStatus.UNKNOWN,
            isServerReachable = false,
            loginStatus = LoginStatus.Undefined
        )
    }
}

fun SessionState.isOK(): Boolean {
    // TODO rather also rely on server connection to also take prefs limit in account
    return isServerReachable && networkStatus == NetworkStatus.OK && loginStatus.isConnected()
}
