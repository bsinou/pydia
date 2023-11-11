package org.sinou.pydia.sdk.api;

/**
 * Credentials based on username and password. Rather use OAuth flows or personal access token.
 */
public interface PasswordCredentials extends Credentials {

    String getPassword();

}
