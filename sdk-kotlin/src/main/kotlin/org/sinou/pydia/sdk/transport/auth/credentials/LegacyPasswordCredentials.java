package org.sinou.pydia.sdk.transport.auth.credentials;

import org.sinou.pydia.sdk.api.PasswordCredentials;

public class LegacyPasswordCredentials implements PasswordCredentials {

    private final String login;
    private final String password;

    public LegacyPasswordCredentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public String getType() {
        return TYPE_LEGACY_PASSWORD;
    }

    @Override
    public String getEncodedValue() {
        return getPassword();
    }

    @Override
    public String getPassword() {
        return password;
    }

}
