package org.sinou.android.pydia.data.ktx

import androidx.annotation.WorkerThread

class AccountRepository(private val accountDao: AccountDao) {

    val accounts : List<Account> = accountDao.getAllAccounts()

    @WorkerThread
    suspend fun insert(account: Account) {
        accountDao.insert(account)
    }

    @WorkerThread
    suspend fun update(account: Account) {
        accountDao.update(account)
    }
}
