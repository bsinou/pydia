package org.sinou.android.pydia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.sinou.android.pydia.auth.ServerUrlFragmentDirections
import org.sinou.android.pydia.databinding.ActivityAccountBinding
import org.sinou.android.pydia.databinding.ActivityAuthBinding

/**
 * Centralizes authentication processes.
 */
class AuthActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val tag = "AuthActivity"

    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)
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

    fun handleIntent(inIntent: Intent) {
        if (Intent.ACTION_VIEW == intent.action) {
            val uri = inIntent.data ?: return
            val code = uri.getQueryParameter(AppNames.KEY_CODE)
            val state  = uri.getQueryParameter(AppNames.KEY_STATE)

            if (code != null && state != null){
                val action = ServerUrlFragmentDirections.actionServerUrlToOauthFlow(null)
                findNavController(R.id.auth_fragment_host).navigate(action)
            }

            launch {
                // CellsApp.instance.accountService.handleOAuthResponse(state, code)
            }
        }
    }

    val onMenuItemSelected = NavigationView.OnNavigationItemSelectedListener {
        Log.i(tag, "... Item selected: #${it.itemId}")
        var done = true
        when (it.itemId) {
            R.id.home_destination -> startActivity(Intent(this, MainActivity::class.java))
            else -> done = NavigationUI.onNavDestinationSelected(it, navController)
        }
        done
    }
}
