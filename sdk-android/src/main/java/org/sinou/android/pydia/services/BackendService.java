package org.sinou.android.pydia.services;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.pydio.cells.api.SDKException;
import com.pydio.cells.api.Server;
import com.pydio.cells.api.ServerURL;
import com.pydio.cells.api.Store;
import com.pydio.cells.api.Transport;
import com.pydio.cells.legacy.P8Server;
import com.pydio.cells.transport.CellsServer;
import com.pydio.cells.transport.ClientData;
import com.pydio.cells.transport.ServerURLImpl;
import com.pydio.cells.transport.auth.CredentialService;
import com.pydio.cells.transport.auth.Token;
import com.pydio.cells.utils.MemoryStore;

import org.sinou.android.pydia.auth.Database;
import org.sinou.android.pydia.auth.DbTokenStore;
import org.sinou.android.pydia.auth.OAuthCallbackManager;
import org.sinou.android.pydia.auth.PasswordStore;
import org.sinou.android.pydia.model.Session;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BackendService {
    private boolean ready = false;
    private final Context context;

    private final Map<String, BackendServicesReadyStateListener> stateListeners = new ConcurrentHashMap<>();

    /* DBs */
    private final String dbFolderPath;
    private final String mainDB = "database.sqlite";
    private final String pollBufferDB = "poll_buffer.sqlite";
    private final String thumbDB = "thumbs.sqlite";
    private final String syncOpDB = "sync_operations.sqlite";
    private final String syncBufferDB = "sync_buffer.sqlite";
    private final String syncTreeDB = "sync_tree.sqlite";
    private final String syncDB = "sync.sqlite";
    private final String cacheDb = "cache_database.sqlite";

    /* Files */

    /* Stores */
    private final Store<Token> tokenStore;
    private final Store<String> passwordStore;
    private final Store<Server> servers;
    private final Store<Transport> transports;

    private final AccountService accountService;
    private final CredentialService credentialService;

    private final SessionFactory appSessionFactory;
    private final OAuthCallbackManager authCallbackManager;

    public BackendService(Context context, String packageName) throws SDKException {

        this.context = context;

        dbFolderPath = baseDir().getPath().concat(File.separator);

        tokenStore = new DbTokenStore();
        passwordStore = new PasswordStore();
        servers = new MemoryStore<>();
        transports = new MemoryStore<>();

        credentialService = new CredentialService(tokenStore, passwordStore);
        accountService = new AccountService();
        authCallbackManager = new OAuthCallbackManager(tokenStore);
        appSessionFactory = new SessionFactory(credentialService, servers, transports, accountService);
        updateClientData(packageName);
    }

    /**
     * Initialises various services. Should be called from a background task.
     * It sets the Ready flag to true upon termination.
     */
    public void afterCreate() {
        // FIXME we insure this is always call (init are idempotent) to avoid
        //  NPEs seen on deployed devices
        initMainDB();
        appSessionFactory.loadKnownAccounts();

        if (ready) {
            publishReadyState();
            return;
        }

        ready = true;
        publishReadyState();
    }

    public void migrate(int oldVersion) throws SDKException {    }

    public boolean isReady() {
        return ready;
    }

    public void publishReadyState() {
        for (String key : stateListeners.keySet()) {
            Objects.requireNonNull(stateListeners.get(key)).onReadyStateChanged(ready);
        }
    }

    public String addReadyStateChangeListener(BackendServicesReadyStateListener listener) {
        String id = UUID.randomUUID().toString();
        stateListeners.put(id, listener);
        return id;
    }

    public void removeReadyStateChangeListener(String id) {
        stateListeners.remove(id);
    }

    public SessionFactory getClientFactory() {
        return appSessionFactory;
    }

    public OAuthCallbackManager getOAuthCallbackManager() {
        return authCallbackManager;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public Session findSession(String id) {
        if (!ready) {
            return null;
        }
        Session session = appSessionFactory.getSession(id);
        if (session == null) {
            Log.d("Loading", "No session found for id " + id);
        } else {
            Log.d("Loading", "Session found for " + id + ", connected: " + session.isOnline());
        }
        return session;
    }

    public Session unlockSession(String id) throws SDKException {
        if (!ready) {
            return null;
        }
        return appSessionFactory.unlockSession(id);
    }

    private File baseDir() {
        return context.getFilesDir();
    }

    private void updateClientData(String packageName) throws SDKException {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new SDKException("Could not retrieve PackageInfo for " + packageName, e);
        }

        // TODO make this more dynamic
        ClientData.clientID = "cells-mobile";
        ClientData.clientSecret = "";

        ClientData.packageID = packageName;
        ClientData.name = "AndroidClient";
        ClientData.version = packageInfo.versionName;
        ClientData.versionCode = packageInfo.versionCode;
        ClientData.platform = getAndroidVersion();
    }

    private String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "AndroidSDK" + sdkVersion + "v" + release;
    }


    private void initMainDB() {
        Database.init(context, dbFolderPath.concat(mainDB));
    }
}
