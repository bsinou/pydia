package org.sinou.android.pydia.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.sinou.android.pydia.data.Account;

import java.util.List;

public class AccountViewModel extends ViewModel {

    private MutableLiveData<List<Account>> accounts;
    public LiveData<List<Account>> getAccounts() {
        if (accounts == null) {
            accounts = new MutableLiveData<List<Account>>();
            loadAccounts();
        }
        return accounts;
    }

    private void loadAccounts() {
        // Do an asynchronous operation to fetch users.
    }

}
