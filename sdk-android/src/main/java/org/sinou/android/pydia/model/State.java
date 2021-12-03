package org.sinou.android.pydia.model;

import androidx.annotation.NonNull;

import com.pydio.cells.api.SdkNames;
import com.pydio.cells.api.ui.Node;
import com.pydio.cells.api.ui.WorkspaceNode;
import com.pydio.cells.transport.StateID;
import com.pydio.cells.utils.Str;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class State implements Serializable {

    private String encodedStateId;

    public static State fromAccountId(String accountId) {
        State state = new State();
        state.encodedStateId = accountId;
        return state;
    }

    public static State fromEncodedState(String encodedState) {
        if (Str.empty(encodedState)) {
            return null;
        }
        State state = new State();
        state.encodedStateId = encodedState;
        return state;
    }

    public static List<State> fromEncodedStates(List<String> encodedStates) {
        List<State> states = new ArrayList<>();
        for (String currEncoded : encodedStates) {
            states.add(fromEncodedState(currEncoded));
        }
        return states;
    }

    public static List<String> toEncodedStates(String accountId, List<Node> nodes) {
        List<String> states = new ArrayList<>();
        for (Node currNode : nodes) {
            states.add((new State(accountId, currNode)).toString());
        }
        return states;
    }

    public State(String accountId, String workspace) {
        StateID stateID = StateID.fromId(accountId);
        stateID = new StateID(stateID.getUsername(), stateID.getServerUrl(), "/" + workspace);
        this.encodedStateId = stateID.getId();
    }

    public State(String accountId, Node node) {
        StateID stateID = StateID.fromId(accountId);

        String slug;
        String file = "";

        switch (node.getType()) {
            case Node.TYPE_WORKSPACE:
                slug = ((WorkspaceNode) node).getSlug();
                break;
            case Node.TYPE_REMOTE_NODE:
                slug = node.getProperty(SdkNames.NODE_PROPERTY_WORKSPACE_SLUG);
                if (slug == null || "".equals(slug)) {
                    slug = node.getProperty(SdkNames.NODE_PROPERTY_WORKSPACE_UUID);
                }
                if (slug == null || "".equals(slug)) {
                    throw new RuntimeException("cannot get workspace slug for " + node.toString());
                }
                // file = utf8Encode(((FileNode) node).getPath());
                file = node.getPath();
                break;
            default:
                throw new RuntimeException("could not create state for node of type " + node.getType());
        }

        stateID = new StateID(stateID.getUsername(), stateID.getServerUrl(), "/" + slug + file);
        this.encodedStateId = stateID.getId();
    }

    public String getAccountID() {
        StateID stateID = StateID.fromId(encodedStateId);
        return new StateID(stateID.getUsername(), stateID.getServerUrl()).getId();
    }

    public String getUsername() {
        StateID stateID = StateID.fromId(encodedStateId);
        return stateID.getUsername();
    }

    public String getServerUrl() {
        StateID stateID = StateID.fromId(encodedStateId);
        return stateID.getServerUrl();
    }

    public String getWorkspace() {
        StateID stateID = StateID.fromId(encodedStateId);
        return stateID.getWorkspace();
    }

    public String getFile() {
        StateID stateID = StateID.fromId(encodedStateId);
        return stateID.getFile();
    }

    public String getPath() {
        StateID stateID = StateID.fromId(encodedStateId);
        return stateID.getPath();
    }

    @NonNull
    public String toString() {
        if (Str.empty(encodedStateId)) {
            return "NaN";
        }
        return encodedStateId;
    }

    /**
     * This is only used for persistence. Do not call directly.
     */
    public State() {
    }

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (object instanceof State) {
            return ((State) object).encodedStateId.equals(encodedStateId);
        }

        return false;
    }

}
