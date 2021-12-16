package org.sinou.android.pydia.model;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.sinou.android.pydia.data.Account;
import org.sinou.android.pydia.data.AccountRepository;

import java.util.List;

public class AccountViewModel extends AndroidViewModel {

    private AccountRepository repository;

    private LiveData<List<Account>> accounts;


    public AccountViewModel (Application application) {
        super(application);
        repository = new AccountRepository(application);
        accounts = repository.listAccounts();
    }

    public LiveData<List<Account>> getAccounts() {
        return accounts;
    }

    public void insert(Account account) { repository.insert(account); }


    private void loadAccounts() {
        // Do an asynchronous operation to fetch users.
    }

}
