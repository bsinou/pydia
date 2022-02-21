package org.sinou.android.pydia.transfer

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.ChooseTargetActivity

class ChooseTargetContract : ActivityResultContract<Pair<StateID, String>, StateID?>() {

    // TODO rather start with a _parent_ State
    override fun createIntent(context: Context, input: Pair<StateID, String>): Intent {
        val intent = Intent(context, ChooseTargetActivity::class.java)
        intent.action = AppNames.ACTION_CHOOSE_TARGET
        intent.putExtra(AppNames.EXTRA_STATE, input.first.id)
        intent.putExtra(AppNames.EXTRA_ACTION_CONTEXT, input.second)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): StateID? {
        return when (resultCode) {
            RESULT_OK -> StateID.fromId(intent?.getStringExtra(AppNames.EXTRA_STATE))
            else -> null
        }
    }
}
