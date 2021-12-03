package org.sinou.android.pydia.services;


import com.pydio.cells.api.SDKException;

import org.sinou.android.pydia.App;
import org.sinou.android.pydia.auth.Database;
import org.sinou.android.pydia.model.AccountRecord;
import org.sinou.android.pydia.model.State;

import java.io.File;
import java.util.List;

/**
 * Simply wraps access to the persistence layer to ease DB refactoring
 */
public class AccountService {

    private final String workingDir;

    public AccountService() {
        // Cannot use path objects until we are at version 29
        // workingDir = Application.baseDir().toPath()
        workingDir = App.baseDir().getPath();
    }

    /**
     * Loads all the records that have been persisted.
     */
    public List<AccountRecord> loadPersistedAccounts() {
        return Database.listAccountRecords();
    }

    public void registerAccount(AccountRecord account) throws SDKException {
        createLocalFolders(account.id());
        Database.saveAccount(account);
    }

    public void unregisterAccount(String id) throws SDKException {
        Database.deleteAccountRecord(id);
        removeLocalFolders(id);
    }

    public void updateAccount(AccountRecord account) throws SDKException {
        Database.saveAccount(account);
    }

    public void createLocalFolders(String accountID) throws SDKException {

        String baseFolderPath = baseLocalFolderPath(accountID);
        File file = new File(baseFolderPath);
        if (!file.exists() && !file.mkdirs()) {
            throw new SDKException("could not create session base directory");
        }

        String cacheFolderPath = cacheLocalFolderPath(accountID);
        file = new File(cacheFolderPath);
        if (!file.exists() && !file.mkdirs()) {
            throw new SDKException("could not create cache directory");
        }

        String tempFolderPath = tempLocalFolderPath(accountID);
        file = new File(tempFolderPath);
        if (!file.exists() && !file.mkdirs()) {
            throw new SDKException("could not create temp directory");
        }
    }

    public void removeLocalFolders(String accountID) throws SDKException {
        File file = new File(baseLocalFolderPath(accountID));
        if (file.exists()) {
            file.delete();
        }
    }

    public String baseLocalFolderPath(String sessionID) {
        return workingDir.concat(File.separator).concat(sessionID);
    }

    public String cacheLocalFolderPath(String sessionID) {
        return baseLocalFolderPath(sessionID).concat(File.separator).concat("_cache");
    }

    public String tempLocalFolderPath(String sessionID) {
        return baseLocalFolderPath(sessionID).concat(File.separator).concat("_temp");
    }

    public String localWsPath(String sessionID, String ws) {
        return baseLocalFolderPath(sessionID).concat(File.separator).concat(ws);
    }

    public String localDownloadPath(State state) {
        String localPath = localWsPath(state.getAccountID(), state.getWorkspace());
        return localPath.concat(state.getFile());
    }

    public String localDownloadPath(String sessionID, String ws, String file) {
        return localWsPath(sessionID, ws).concat(file);
    }

    public boolean isAlreadyCached(State state) {
        return new File(localDownloadPath(state)).exists();
    }
}
