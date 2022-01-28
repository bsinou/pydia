package org.sinou.android.pydia.services

import androidx.lifecycle.LiveData
import org.sinou.android.pydia.db.runtime.RUpload
import org.sinou.android.pydia.db.runtime.RuntimeDB

class TransferService(val runtimeDB: RuntimeDB) {

    private val tag = TransferService::class.java.simpleName

    val activeTransfers: LiveData<List<RUpload>?> = runtimeDB.uploadDao().getActiveTransfers()
}
