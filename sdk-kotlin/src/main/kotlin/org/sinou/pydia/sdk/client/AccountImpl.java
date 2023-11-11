package org.sinou.pydia.sdk.client;

import org.sinou.pydia.sdk.api.IAccount;
import org.sinou.pydia.sdk.api.ServerURL;

public class AccountImpl implements IAccount {

    private String login;
    private ServerURL serverURL;

    @Override
    public ServerURL getServerURL() {
        return null;
    }

    @Override
    public String getLogin() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }
}
