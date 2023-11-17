package org.sinou.pydia.client.core.ui.login

import org.sinou.pydia.client.core.AppKeys
import org.sinou.pydia.client.core.ui.core.encodeStateForRoute
import org.sinou.pydia.sdk.transport.StateID

sealed class LoginDestinations(val route: String) {

    companion object {
        const val PREFIX = "login"
        fun isCurrent(route: String?): Boolean = route?.startsWith(PREFIX) ?: false
    }

    data object Starting : LoginDestinations("$PREFIX/starting/{${AppKeys.STATE_ID}}") {
        fun createRoute(stateID: StateID) = "$PREFIX/starting/${encodeStateForRoute(stateID)}"
        fun isCurrent(route: String?): Boolean =
            route?.startsWith("$PREFIX/starting/") ?: false
    }

    data object Done : LoginDestinations("$PREFIX/done/{${AppKeys.STATE_ID}}") {
        fun createRoute(stateID: StateID) = "$PREFIX/done/${encodeStateForRoute(stateID)}"
        fun isCurrent(route: String?): Boolean = route?.startsWith("$PREFIX/done/") ?: false
    }

    data object AskUrl : LoginDestinations("$PREFIX/ask-url") {

        fun createRoute() = "$PREFIX/ask-url"

        fun isCurrent(route: String?): Boolean =
            route?.startsWith("$PREFIX/ask-url") ?: false
    }

    data object SkipVerify : LoginDestinations("$PREFIX/skip-verify/{${AppKeys.STATE_ID}}") {

        fun createRoute(stateID: StateID) = "$PREFIX/skip-verify/${encodeStateForRoute(stateID)}"

        fun isCurrent(route: String?): Boolean =
            route?.startsWith("$PREFIX/skip-verify/") ?: false
    }

    data object P8Credentials :
        LoginDestinations(
            "$PREFIX/p8-credentials/" +
                    "{${AppKeys.STATE_ID}}/{${AppKeys.SKIP_VERIFY}}/{${AppKeys.LOGIN_CONTEXT}}"
        ) {

        fun createRoute(stateID: StateID, skipVerify: Boolean, loginContext: String) =
            "$PREFIX/p8-credentials/${encodeStateForRoute(stateID)}/$skipVerify/$loginContext"

        fun isCurrent(route: String?): Boolean =
            route?.startsWith("$PREFIX/p8-credentials/") ?: false
    }


    data object LaunchAuthProcessing :
        LoginDestinations(
            "$PREFIX/launch-auth-processing/" +
                    "{${AppKeys.STATE_ID}}/{${AppKeys.SKIP_VERIFY}}/{${AppKeys.LOGIN_CONTEXT}}"
        ) {

        fun createRoute(stateID: StateID, skipVerify: Boolean, loginContext: String) =
            "$PREFIX/launch-auth-processing/${encodeStateForRoute(stateID)}/$skipVerify/$loginContext"

        fun isCurrent(route: String?): Boolean =
            route?.startsWith("$PREFIX/launch-auth-processing/") ?: false
    }

    data object ProcessAuthCallback :
        LoginDestinations("$PREFIX/process-auth-callback/{${AppKeys.STATE_ID}}") {

        fun createRoute(stateID: StateID) =
            "$PREFIX/process-auth-callback/${encodeStateForRoute(stateID)}"

        fun isCurrent(route: String?): Boolean =
            route?.startsWith("$PREFIX/process-auth-callback/") ?: false
    }
}
