package org.sinou.android.pydia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.sinou.android.pydia.databinding.ActivityAuthBinding
import org.sinou.android.pydia.ui.auth.ServerUrlFragmentDirections

/**
 * Centralizes authentication processes.
 */
class AuthActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val tag = "AuthActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityAuthBinding>(this, R.layout.activity_auth)
    }

    override fun onResume() {
        Log.i(tag, "onResume, intent: $intent")
        super.onResume()
        handleIntent(intent)
    }

    override fun onPause() {
        Log.i(tag, "onPause, intent: $intent")
        super.onPause()
    }

    private fun handleIntent(inIntent: Intent) {
        if (Intent.ACTION_VIEW == intent.action) {
            val uri = inIntent.data ?: return
            val code = uri.getQueryParameter(AppNames.QUERY_KEY_CODE)
            val state = uri.getQueryParameter(AppNames.QUERY_KEY_STATE)

            if (code != null && state != null) {
                val action = ServerUrlFragmentDirections.actionServerUrlToOauthFlow(null)
                findNavController(R.id.auth_fragment_host).navigate(action)
                return
            }
        }

        if (intent.hasExtra(AppNames.EXTRA_SERVER_URL)) {
            val urlStr: String = intent.getStringExtra(AppNames.EXTRA_SERVER_URL)!!
            if (intent.getBooleanExtra(AppNames.EXTRA_SERVER_IS_LEGACY, false)) {
                val action = ServerUrlFragmentDirections.actionServerUrlToP8Creds(
                    urlStr,
                    intent.getStringExtra(AppNames.EXTRA_AFTER_AUTH_ACTION)!!
                )
                findNavController(R.id.auth_fragment_host).navigate(action)
            } else {
                val action = ServerUrlFragmentDirections.actionServerUrlToOauthFlow(urlStr)
                findNavController(R.id.auth_fragment_host).navigate(action)
            }
        }
    }
}
