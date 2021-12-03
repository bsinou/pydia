package org.sinou.android.pydia.auth;

import com.pydio.cells.api.Store;
import com.pydio.cells.transport.auth.Token;

import java.util.Map;

/**
 * Simply wraps the database to ease later refactoring
 */
public class DbTokenStore implements Store<Token> {

    @Override
    public void put(String id, Token token) {
        Database.saveToken(id, token);
    }

    /**
     * Returns a saved token from the underlying DB if found.
     * This method does *not* check if the token is expired, this is caller responsibility
     * because it has access to a transport and potentially a Password store.
     *
     * @param id an account ID
     * @return a Token, if any has been found or null otherwise
     */
    @Override
    public Token get(String id) {
        return Database.getToken(id);
    }

    @Override
    public void remove(String id) {
        Database.deleteToken(id);
    }

    @Override
    public void clear() {
        // We do not want to enable emptying the token DB oder ?
        throw new RuntimeException("Forbidden call");
    }

    @Override
    public Map<String, Token> getAll() {
        // We do not want to enable token listing
        //         return Database.listAllTokens();
        throw new RuntimeException("Forbidden call");
    }

}
