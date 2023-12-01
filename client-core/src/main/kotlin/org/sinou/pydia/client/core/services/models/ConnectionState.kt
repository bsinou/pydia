package org.sinou.pydia.client.core.services.models

import org.sinou.pydia.client.core.LoadingState
import org.sinou.pydia.client.core.ServerConnection

data class ConnectionState(val loading: LoadingState, val serverConnection: ServerConnection)