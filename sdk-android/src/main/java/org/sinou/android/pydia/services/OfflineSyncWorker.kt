package org.sinou.android.pydia.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class OfflineSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {


        TODO("Not yet implemented")
    }
}