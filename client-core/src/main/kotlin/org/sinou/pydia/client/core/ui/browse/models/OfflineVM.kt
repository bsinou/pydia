package org.sinou.pydia.client.core.ui.browse.models

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import org.sinou.pydia.client.core.AppNames
import org.sinou.pydia.client.core.ServerConnection
import org.sinou.pydia.client.core.db.nodes.RLiveOfflineRoot
import org.sinou.pydia.client.core.db.preferences.defaultCellsPreferences
import org.sinou.pydia.client.core.db.runtime.RJob
import org.sinou.pydia.client.core.services.ConnectionService
import org.sinou.pydia.client.core.services.JobService
import org.sinou.pydia.client.core.services.OfflineService
import org.sinou.pydia.client.core.services.TransferService
import org.sinou.pydia.client.core.ui.core.AbstractCellsVM
import org.sinou.pydia.sdk.transport.StateID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Expose methods used by Offline pages */
class OfflineVM(
    stateID: StateID,
    private val connectionService: ConnectionService,
    private val jobService: JobService,
    private val transferService: TransferService,
    private val offlineService: OfflineService,
) : AbstractCellsVM() {

    private val logTag = "OfflineVM"

    private val accountID = stateID.account()

    private val _syncJobID = MutableStateFlow(-1L)

    // Observe the defined offline roots for current account
    @OptIn(ExperimentalCoroutinesApi::class)
    val offlineRoots: StateFlow<List<RLiveOfflineRoot>> =
        defaultOrderPair.flatMapLatest { currPair ->
            nodeService.listOfflineRoots(accountID, currPair.first, currPair.second)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )

    // Observe latest sync job
    @OptIn(ExperimentalCoroutinesApi::class)
    val syncJob: StateFlow<RJob?> = _syncJobID.flatMapLatest { passedID ->
        // Not satisfying, if the job ID is not explicitly given, we retrieve the latest not done from the DB
        val currID = if (passedID < 1) {
            jobService.getLatestRunning(offlineService.getSyncTemplateId(accountID))?.jobId ?: -1
        } else {
            passedID
        }
        jobService.getLiveJobByID(currID)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun download(stateID: StateID, uri: Uri) {
        viewModelScope.launch {
            try {
                transferService.saveToSharedStorage(stateID, uri)
                done()
            } catch (e: Exception) {
                done(e)
            }
        }
    }

    fun removeFromOffline(stateID: StateID) {
        viewModelScope.launch {
            try {
                offlineService.toggleOffline(stateID, false)
            } catch (e: Exception) {
                done(e)
            }
        }
    }

    fun forceFullSync() {
        viewModelScope.launch {
            try {
                if (!checkBeforeLaunch(accountID)) {
                    return@launch
                }
                doForceAccountSync(accountID) // we insure the current account value is valid in the sanity check
                delay(1500)
                Log.i(logTag, "Setting loading state to IDLE")
                done()
            } catch (e: Exception) {
                done(e)
            }
        }
    }

    fun forceSync(stateID: StateID) {
        viewModelScope.launch {
            if (!checkBeforeLaunch(stateID)) {
                return@launch
            }
            try {
                _syncJobID.value = offlineService.launchOfflineRootSync(stateID)
                done()
            } catch (e: Exception) {
                done(e)
            }
        }
    }

    private suspend fun checkBeforeLaunch(stateID: StateID): Boolean {
        val (ok, msg) = canLaunchSync(stateID.account())
        if (ok) {
            launchProcessing()
        } else {
            msg?.let { error(it) }
            return false
        }
        return true
    }

    private suspend fun canLaunchSync(stateID: StateID?): Pair<Boolean, String?> {

        val offlinePrefs = try {
            prefs.fetchPreferences()
        } catch (e: IllegalArgumentException) {
            defaultCellsPreferences()
        }

        return when (connectionService.liveConnectionState.value.serverConnection) {
            ServerConnection.OK -> {
                return stateID?.let {
                    if (it != StateID.NONE) {
                        Pair(true, null)
                    } else {
                        Pair(false, "Cannot launch re-sync without choosing a target")
                    }
                } ?: Pair(false, "Cannot launch re-sync without choosing a target")
            }

            ServerConnection.LIMITED -> {
                // TODO make better checks
                return if (offlinePrefs.meteredNetwork.applyLimits) {
                    Pair(false, "Preventing re-sync on metered network")
                } else {
                    return stateID?.let {
                        if (it != StateID.NONE) {
                            Pair(true, null)
                        } else {
                            Pair(false, "Cannot launch re-sync without choosing a target")
                        }
                    } ?: Pair(false, "Cannot launch re-sync without choosing a target")
                }
            }

            ServerConnection.UNREACHABLE -> {
                // is NetworkStatus.Unavailable, is NetworkStatus.Captive, is NetworkStatus.Unknown -> {
                Pair(false, "Cannot launch re-sync when server is un-reachable")
            }
        }
    }

    private suspend fun doForceAccountSync(accountID: StateID) {
        val jobID = offlineService.prepareAccountSync(accountID, AppNames.JOB_OWNER_USER)
        Log.e(logTag, "Account sync prepared, with job #$jobID")

        _syncJobID.value = jobID
        jobService.launched(jobID)
        offlineService.performAccountSync(
            accountID,
            jobID
        )
    }

    init {
        done()
    }
}
