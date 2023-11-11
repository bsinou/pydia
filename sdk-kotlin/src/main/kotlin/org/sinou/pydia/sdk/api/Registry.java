package org.sinou.pydia.sdk.api;

import org.sinou.pydia.sdk.api.ui.Plugin;
import org.sinou.pydia.sdk.api.ui.WorkspaceNode;
import org.sinou.pydia.sdk.client.model.Action;

import java.util.List;

/**
 * The registry holds info about workspaces, actions and plugins that are available on a remote Pydio Server.
 * <p>
 * Tip: you can view the registry of your Pydio server via the developer console of your web browser.
 * Just type and execute the following javascript function: {@code pydio.getXmlRegistry();}
 */
public interface Registry {

    List<WorkspaceNode> getWorkspaces();

    List<Action> getActions();

    List<Plugin> getPlugins();

    boolean isLoggedIn();
}
