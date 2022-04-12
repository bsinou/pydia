package org.sinou.android.pydia.tasks

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.pydio.cells.transport.ServerURLImpl
import kotlinx.coroutines.launch
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.AuthActivity
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.accounts.RLiveSession
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.AuthService
import org.sinou.android.pydia.services.SessionFactory

fun loginAccount(
    context: Context,
    authService: AuthService,
    sessionFactory: SessionFactory,
    session: RLiveSession,
    next: String,
): Boolean {

    CellsApp.instance.appScope.launch {

        // TODO clean this when implementing custom certificate acceptance.
        val serverURL = ServerURLImpl.fromAddress(session.url, session.tlsMode == 1)

        if (session.isLegacy) {
            val toAuthIntent = Intent(context, AuthActivity::class.java)
            toAuthIntent.putExtra(AppNames.EXTRA_SERVER_URL, serverURL.toJson())
            toAuthIntent.putExtra(AppNames.EXTRA_SERVER_IS_LEGACY, true)
            toAuthIntent.putExtra(AppNames.EXTRA_AFTER_AUTH_ACTION, next)
            startActivity(context, toAuthIntent, null)

        } else {
            authService.createOAuthIntent(
                sessionFactory, serverURL, next
            )?.let {
                startActivity(context, it, null)
            } ?: run {
                Log.e("LoginAccount()", "Could not create OAuth intent for ${serverURL.url}")
            }
        }
    }

    return true
}
