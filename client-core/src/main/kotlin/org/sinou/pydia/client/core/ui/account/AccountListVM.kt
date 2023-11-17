package org.sinou.pydia.client.core.ui.account

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sinou.pydia.client.core.db.accounts.RSessionView
import org.sinou.pydia.client.core.services.AccountService
import org.sinou.pydia.client.core.ui.core.AbstractCellsVM
import org.sinou.pydia.client.core.utils.BackOffTicker
import org.sinou.pydia.sdk.transport.StateID
import java.util.concurrent.TimeUnit

/**
 * Central ViewModel when dealing with a user's accounts.
 */
class AccountListVM(
    private val accountService: AccountService,
) : AbstractCellsVM() {

    private val logTag = "AccountListVM"

    val sessions = accountService.getLiveSessions()

    private val backOffTicker = BackOffTicker()
    private var _isActive = false
    private var currWatcher: Job? = null

    suspend fun getSession(stateID: StateID): RSessionView? {
        return accountService.getSession(stateID)
    }

    fun watch() {
        setLoading(true)
        pause()
        resume()
    }

    private fun resume() {
        backOffTicker.resetIndex()
        if (!_isActive) {
            _isActive = true
            currWatcher = watchAccounts()
        }
    }

    fun pause() {
        currWatcher?.cancel()
        _isActive = false
        setLoading(false)
        Log.i(logTag, "... Remote **ACCOUNT** watching paused.")
    }

    fun forgetAccount(stateID: StateID) {
        viewModelScope.launch {
            accountService.forgetAccount(stateID.account())
        }
    }

    suspend fun openSession(stateID: StateID): RSessionView? {
        return accountService.openSession(stateID)
    }

    fun logoutAccount(stateID: StateID) {
        viewModelScope.launch {
            accountService.logoutAccount(stateID.account())
        }
    }

    // Local helpers
    private fun watchAccounts() = viewModelScope.launch {
        while (_isActive) {
            doCheckAccounts()
            val nd = backOffTicker.getNextDelay()
            Log.d(logTag, "... Watching accounts, next delay: ${nd}s")
            delay(TimeUnit.SECONDS.toMillis(nd))
        }
        Log.i(logTag, "pausing the account watch process")
    }

    private suspend fun doCheckAccounts() {
        // First we check if some token are about to expire
        // accountService.checkRegisteredAccounts()
        // Then we refresh the list
        try {
            val changes = accountService.checkRegisteredAccounts()
            if (changes > 0) {
                Log.i(logTag, "Found $changes change(s)")
                backOffTicker.resetIndex()
            }
            done()
        } catch (e: Exception) {
            // Small hack: we skip the message if it happens just after a backoff ticker reset
            // TODO comes from when we did not handle network correctly. Insure it is still necessary
            if (backOffTicker.getCurrentIndex() > 0) {
                done(e)
            } else {
                done()
            }
            pause()
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            launchProcessing()
        }
    }

    override fun onCleared() {
        // viewModelJob.cancel()
        super.onCleared()
        Log.i(logTag, "Cleared")
    }

    init {
        setLoading(true)
    }
}
