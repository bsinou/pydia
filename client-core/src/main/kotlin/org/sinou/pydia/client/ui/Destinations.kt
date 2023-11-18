package org.sinou.pydia.client.ui

import org.sinou.pydia.client.ui.core.encodeStateForRoute
import org.sinou.pydia.sdk.transport.StateID

class Destinations {

    companion object {
        fun browse(stateID: StateID): String {
            // TODO make path modular
            return "browse/open/${encodeStateForRoute(stateID)}"
        }
    }
}