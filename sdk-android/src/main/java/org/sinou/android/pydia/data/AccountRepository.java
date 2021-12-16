package org.sinou.android.pydia.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class AccountRepository {

    private AccountDao accountDao;
    private LiveData<List<Account>> allAccounts;

    public AccountRepository(Application application) {
        AccountDatabase db = AccountDatabase.getDatabase(application);
        accountDao = db.accountDao();
        allAccounts = accountDao.listAccounts();
    }

    public LiveData<List<Account>> listAccounts() {
        return allAccounts;
    }


    public void insert(Account word) {
        new insertAsyncTask(accountDao).execute(word);
    }

    private static class insertAsyncTask extends AsyncTask<Account, Void, Void> {

        private AccountDao accountDao;

        insertAsyncTask(AccountDao dao) {
            accountDao = dao;
        }

        @Override
        protected Void doInBackground(final Account... params) {
            accountDao.insert(params[0]);
            return null;
        }
    }
}
