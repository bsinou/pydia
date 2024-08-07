package org.sinou.pydia.client.ui.share

import org.sinou.pydia.client.core.AppKeys
import org.sinou.pydia.client.ui.core.encodeStateForRoute
import org.sinou.pydia.sdk.transport.StateID

sealed class ShareDestinations(val route: String) {

    companion object {
        protected const val PREFIX = "share"
        fun isCurrent(route: String?): Boolean = route?.startsWith(PREFIX) ?: false
    }

    object ChooseAccount : ShareDestinations("$PREFIX/choose-account") {
        fun isCurrent(route: String?): Boolean = "$PREFIX/choose-account" == route
    }

    object OpenFolder : ShareDestinations("$PREFIX/open/{${AppKeys.STATE_ID}}") {
        fun createRoute(stateID: StateID) = "$PREFIX/open/${encodeStateForRoute(stateID)}"
        fun isCurrent(route: String?): Boolean = route?.startsWith("$PREFIX/open/") ?: false
    }

    object UploadInProgress :
        ShareDestinations("$PREFIX/in-progress/{${AppKeys.STATE_ID}}/{${AppKeys.UID}}") {
        fun createRoute(stateID: StateID, jobID: Long) =
            "$PREFIX/in-progress/${stateID.id}/${jobID}"

        fun isCurrent(route: String?): Boolean =
            route?.startsWith("$PREFIX/in-progress/") ?: false
    }

    // TODO add safety checks to prevent forbidden copy-move
    //  --> to finalise we must really pass the node*s* to copy or move rather than its parent
}
