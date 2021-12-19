package org.sinou.android.pydia.services;

import com.pydio.cells.api.Server;
import com.pydio.cells.api.Store;
import com.pydio.cells.api.Transport;
import com.pydio.cells.client.CellsClient;
import com.pydio.cells.client.ClientFactory;
import com.pydio.cells.transport.CellsTransport;
import com.pydio.cells.transport.auth.CredentialService;

import org.sinou.android.pydia.room.account.AccountDB;

public class SessionFactory_old extends ClientFactory {


    private final AccountDB accountDB;

/*
    private final AccountService accountService;
    private final CredentialService credentialService;
*/

    public SessionFactory_old(AccountDB accountDB, CredentialService credentialService, Store<Server> serverStore, Store<Transport> transportStore) {
        super(credentialService, serverStore, transportStore);
    this.accountDB = accountDB;
    }


    @Override
    protected CellsClient getCellsClient(CellsTransport transport) {
        return new CellsClient(transport, new S3Client(transport));
    }

//
//
//
//    class PasswordStore implements Store<String> {
//
//        @Override
//        public void put(String id, String password) {
//            Database.addPassword(id, password);
//        }
//
//        @Override
//        public String get(String id) {
//            return Database.password(id);
//        }
//
//        @Override
//        public void remove(String id) {
//            Database.deletePassword(id);
//        }
//
//        @Override
//        public void clear() {
//            // We do not want to enable emptying the password DB oder ?
//            throw new RuntimeException("Forbidden call");
//        }
//
//        @Override
//        public Map<String, String> getAll() {
//            // We do not want to enable token listing
//            //         return Database.listAllTokens();
//            throw new RuntimeException("Forbidden call");
//        }
//
//    }



}
//
//
//    // Locally store a cache of known sessions
//    private final Store<Session> sessions = new MemoryStore<>();
//
//
//
//    public void loadKnownAccounts() {
//        List<AccountRecord> records = accountService.loadPersistedAccounts();
//        sessions.clear();
//        for (AccountRecord record : records) {
//            Session session = new Session(record);
//            sessions.put(record.id(), session);
//        }
//    }
//
//    @Override
//    public void unregisterAccount(String id) throws SDKException {
//        // TODO Make this more robust
//        accountService.unregisterAccount(id);
//
//        if (sessions.get(id) != null) {
//            sessions.remove(id);
//        }
//
//        super.unregisterAccount(id);
//    }
//
//    public Session registerSession(ServerURL serverURL, Credentials credentials) throws SDKException {
//
//        // TODO better handling of already existing sessions / accounts
//        String accountId = ServerFactory.accountID(credentials.getUsername(), serverURL);
//        // This also stores the password or retrieved token via the CredentialService
//        registerAccountCredentials(serverURL, credentials);
//
//        Server server = getServer(serverURL.getId());
//        AccountRecord account = AccountRecord.fromServer(credentials.getUsername(), server);
//        accountService.registerAccount(account);
//
//        Session session = new Session(account, getTransport(accountId));
//        sessions.put(account.id(), session);
//
//        return session;
//    }
//
//    public Session resurrectSession(AccountRecord accountRecord, Credentials credentials) throws SDKException {
//        ServerURL serverURL;
//        StateID stateID = StateID.fromId(accountRecord.id());
//        try {
//            // TODO also handle trustManager for ServerURL
//            serverURL = ServerURLImpl.fromAddress(stateID.getServerUrl(), accountRecord.skipVerify());
//        } catch (MalformedURLException e) {
//            // Should never happen, URL is sanitized before persistence
//            throw new SDKException(e);
//        }
//        registerAccountCredentials(serverURL, credentials);
//
//        Session session = new Session(accountRecord, getTransport(accountRecord.id()));
//        sessions.put(accountRecord.id(), session);
//        return session;
//    }
//
//    public Session unlockSession(String id) throws SDKException {
//        Session session = sessions.get(id);
//        if (Session.STATUS_ONLINE.equals(session.getStatus())) {
//            return session;
//        }
//
//        session.setStatus(Session.STATUS_LOADING);
//
//        StateID stateID = StateID.fromId(id);
//        ServerURL serverURL;
//        // TODO handle here to have a ServerURL object with a certificate manager.
//        try {
//            serverURL = ServerURLImpl.fromAddress(session.getAccount().url(), session.getAccount().skipVerify());
//        } catch (MalformedURLException me) { // This should never happen here, URL is sanitized before persistence.
//            Log.e("Internal error", "Could not restore account " + id);
//            throw new SDKException(ErrorCodes.panic);
//        }
//
//        Server server = getServer(id);
//        Transport transport = getTransport(id);
//
//        if (transport == null) {
//            server = registerServer(serverURL);
//            Credentials credentials;
//            if (session.isLegacy()) {
//                credentials = new P8Credentials(session.getUser(), Database.password(id));
//                registerAccountCredentials(serverURL, credentials);
//            } else {
//                restoreAccount(serverURL, stateID.getUsername());
//            }
//            transport = getTransport(id);
//        }
//
//        session.setOnline(serverURL, server, transport);
//        return session;
//    }
//
//    public Map<String, Session> getSessions() {
//        return sessions.getAll();
//    }
//
//    public Session getSession(String id) {
//        return sessions.get(id);
//    }
//

//    public Client getUnlockedClient(String accountId) throws SDKException {
//        Session session = unlockSession(accountId);
//        return getClient(session.getTransport());
//    }
//
//    @Override
//    public CustomEncoder getEncoder() {
//        return new AndroidCustomEncoder();
//    }
//}


