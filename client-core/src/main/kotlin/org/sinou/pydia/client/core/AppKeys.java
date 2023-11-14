package org.sinou.pydia.client.core;

/**
 * Centralises application-wide keys.
 */
public interface AppKeys {

    // Extra parameters for compose routes
    String STATE_ID = "state-id";
    String STATE_IDS = "state-ids";
    String UID = "uid";
    String SKIP_VERIFY = "skip-verify";
    String LOGIN_CONTEXT = "login-context";
    String QUERY_CONTEXT = "query-context";
    // Intent keys
    String EXTRA_STATE = AppNames.KEY_PREFIX_ + "state";
}
