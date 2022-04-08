package org.sinou.android.pydia.services

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pydio.cells.utils.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.utils.asFormattedString
import org.sinou.android.pydia.utils.getCurrentDateTime

class OfflineSyncWorker(
    private val accountService: AccountService,
    private val nodeService: NodeService,
    appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "OfflineSyncWorker"
    }

    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {

        Log.e(
            WORK_NAME, "... Launching full re-sync in background at ${
                getCurrentDateTime().asFormattedString("yyyy-MM-dd HH:mm")
            }"
        )

        for (session in accountService.listLiveSessions(false)) {
            if (session.lifecycleState != AppNames.LIFECYCLE_STATE_PAUSED
                && session.authStatus == AppNames.AUTH_STATUS_CONNECTED
            ) {
                nodeService.syncAll(session.getStateID())
            }
        }
        Log.e(
            WORK_NAME, "... Full re-sync terminated successfully at ${
                getCurrentDateTime().asFormattedString("yyyy-MM-dd HH:mm")
            }"
        )
        return Result.Success()
    }
}