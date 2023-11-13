package org.sinou.pydia.sdk.api

import org.sinou.pydia.sdk.api.ui.Plugin
import org.sinou.pydia.sdk.api.ui.WorkspaceNode
import org.sinou.pydia.sdk.client.model.Action

/**
 * The registry holds info about workspaces, actions and plugins that are available on a remote Pydio Server.
 *
 *
 * Tip: you can view the registry of your Pydio server via the developer console of your web browser.
 * Just type and execute the following javascript function: `pydio.getXmlRegistry();`
 */
interface Registry {
    fun getWorkspaces() : List<WorkspaceNode>
    fun getActions(): List<Action>
    fun getPlugins(): List<Plugin>
    fun isLoggedIn(): Boolean
}