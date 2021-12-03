package org.sinou.android.pydia.auth;

import com.pydio.cells.api.Store;

import java.util.Map;

public class PasswordStore implements Store<String> {

    @Override
    public void put(String id, String password) {
        Database.addPassword(id, password);
    }

    @Override
    public String get(String id) {
        return Database.password(id);
    }

    @Override
    public void remove(String id) {
        Database.deletePassword(id);
    }

    @Override
    public void clear() {
        // We do not want to enable emptying the password DB oder ?
        throw new RuntimeException("Forbidden call");
    }

    @Override
    public Map<String, String> getAll() {
        // We do not want to enable token listing
        //         return Database.listAllTokens();
        throw new RuntimeException("Forbidden call");
    }

}
