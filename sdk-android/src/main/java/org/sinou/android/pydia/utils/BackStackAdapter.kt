package org.sinou.android.pydia.utils

import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.CellsApp

/**
 * Tweak back navigation to force target state to be the root of the account when navigating back
 * from the first level, typically, workspace root, bookmarks or accounts, otherwise we will be
 * redirected back to where we are by the logic that is launched base on the state when the
 * workspace list fragment resumes.
 */
class BackStackAdapter(enabled: Boolean = true) : OnBackPressedCallback(enabled) {

    private var accountID: StateID? = null
    private var manager: FragmentManager? = null

    fun initializeBackNavigation(manager: FragmentManager?, stateID: StateID) {
        this.manager = manager
        if (stateID.path == "/${stateID.workspace}") {
            // isEnabled = true
            accountID = StateID.fromId(stateID.accountId)
        }
    }

    override fun handleOnBackPressed() {


        accountID?.let {
            Log.i("BackStackAdapter", "Setting custom state before navigating back")
            CellsApp.instance.setCurrentState(it)
            manager?.popBackStackImmediate()
        }
    }
}