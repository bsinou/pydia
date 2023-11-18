package org.sinou.pydia.client.ui.system.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.sinou.pydia.client.core.services.AccountService
import org.sinou.pydia.client.core.services.AuthService
import org.sinou.pydia.client.core.services.CoroutineService
import org.sinou.pydia.client.core.services.JobService
import org.sinou.pydia.client.core.services.PreferencesService
import org.sinou.pydia.client.ui.Destinations
import org.sinou.pydia.client.ui.StartingState
import org.sinou.pydia.client.ui.core.nav.CellsDestinations
import org.sinou.pydia.client.ui.login.LoginDestinations
import org.sinou.pydia.sdk.transport.ClientData
import org.sinou.pydia.sdk.transport.StateID
import kotlin.properties.Delegates

class LandingVM(
    private val prefs: PreferencesService,
    coroutineService: CoroutineService,
    private val jobService: JobService,
    private val authService: AuthService,
    private val accountService: AccountService,
) : ViewModel() {

    private val logTag = "LandingVM"
    private val ioDispatcher = coroutineService.ioDispatcher

    private var oldVersion by Delegates.notNull<Int>()
    private val newVersion = ClientData.getInstance().versionCode.toInt()

    init {
        viewModelScope.launch {
            oldVersion = prefs.getInstalledVersion()
        }
    }

    override fun onCleared() {
        // useless: this does nothing
        // super.onCleared()
        Log.d(logTag, "About to clear")
    }

    /**
     * Makes a first quick check for the happy path and returns true
     * only if code version is the same as the stored version
     *
     * Note: "false" means that we have to trigger the migrate activity that:
     * - performs advanced tests,
     * - does a migration (if necessary)
     * - updates the stored version number.
     *
     * Note: We also get "false" for fresh installs and go through migration.
     *  It takes a few seconds more to start but  subsequent starts are then faster:
     *  they avoid instantiating legacy migration objects
     */
    suspend fun noMigrationNeeded(): Boolean {
        val currInstalled = prefs.getInstalledVersion()
        return newVersion > 100 && newVersion == currInstalled
    }

    suspend fun isAuthStateValid(state: String): Pair<Boolean, StateID> {
        return authService.isAuthStateValid(state)
    }

    suspend fun getStartingState(): StartingState {
        // TODO get latest known state from preferences and navigate to it

        // Fallback on defined accounts:
        val sessions = accountService.listSessionViews()
        val stateID  = when (sessions.size) {
            0 -> null
            1 -> sessions[0].getStateID()
            else -> {
                // If a session is listed as in foreground, we open this one
                accountService.getActiveSession()?.getStateID() ?: StateID.NONE
            }
        }

        val state = StartingState(stateID ?: StateID.NONE)
        state.route = when (stateID) {
            null -> LoginDestinations.AskUrl.createRoute()
            StateID.NONE -> CellsDestinations.Accounts.route
            else -> Destinations.browse(stateID)
        }
        return state
    }
}
