package org.sinou.android.pydia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LandActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LandActivity"
        const val tickDuration = 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.categories?.contains(Intent.CATEGORY_LAUNCHER) == true) {
            chooseFirstPage()
        }
    }

    private fun chooseFirstPage() {
        val landActivity = this
        lifecycleScope.launch {

            // Wait until necessary services have been initialized
            waitForIt()

            // Get the app last recorded state
            var stateID = CellsApp.instance.getCurrentState()

            if (stateID == null) { // No state or (TODO) last state does not exist anymore

                // Fallback on defined accounts:
                val accounts = withContext(Dispatchers.IO) {
                    CellsApp.instance.accountService.accountDB.accountDao().getAccounts()
                }
                when (accounts.size) {
                    0 -> { // No account: launch registration
                        startActivity(Intent(landActivity, AuthActivity::class.java))
                        landActivity.finish()
                        return@launch
                    }
                    1 -> { // Only one: force state to its root
                        stateID = StateID.fromId(accounts[0].accountID)
                        CellsApp.instance.setCurrentState(stateID)
                    }
                    // else we navigate to the MainActivity with no state,
                    //  that should led us to the account list
                    //  size > 1 -> navController.navigate(MainNavDirections.openAccountList())
                }
            }
            val intent = Intent(landActivity, MainActivity::class.java)
            if (stateID != null) {
                CellsApp.instance.accountService.openSession(stateID.accountId)
                intent.putExtra(AppNames.EXTRA_STATE, stateID.id)
            }
            startActivity(intent)
            landActivity.finish()
        }
    }

    private suspend fun waitForIt() {
        repeat(30) { // we wait at most 30 seconds before crashing
            Log.i(TAG, "Waiting for backend to be ready")
            if (CellsApp.instance.ready) {
                Log.i(TAG, "### Backend is now ready")
                return
                // TODO session factory might not be ready at this point
//
                //                if (CellsApp.instance.accountService.sessionFactory.isReady()) {
//                    Log.i(TAG, "### Backend is now ready")
//                    return
//                }
            }
            delay(tickDuration)
        }
    }
}
