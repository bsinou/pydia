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
import org.sinou.android.pydia.databinding.ActivityAccountBinding

/**
 * Centralizes identification and authentification.
 */
class AccountActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val TAG = "AccountActivity"

    private lateinit var binding: ActivityAccountBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account)
        buildNavigationLayout()
        binding.navView.setNavigationItemSelectedListener(onMenuItemSelected);
    }

    override fun onResume() {
        Log.i(TAG, "onResume, intent: $intent")
        super.onResume()
        handleIntent(intent)
    }

    fun handleIntent(inIntent: Intent) {
        if (Intent.ACTION_VIEW == intent.action) {
            val uri = inIntent.data ?: return
//            if (uri == null) {
//                finish(); return
//            }
            val code: String = uri.getQueryParameter(AppNames.KEY_CODE)!!
            val state: String = uri.getQueryParameter(AppNames.KEY_STATE)!!
            launch {
                CellsApp.instance.accountRepository.handleOAuthResponse(state, code)
            }
        }
    }

    private fun buildNavigationLayout() {

        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar, R.string.nav_open,
            R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navController = findNavController(R.id.account_fragment_host)
        NavigationUI.setupWithNavController(binding.navView, navController)
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            binding.drawerLayout
        )

    }

    val onMenuItemSelected = NavigationView.OnNavigationItemSelectedListener {
        Log.i(TAG, "... Item selected: #${it.itemId}")
        var done = true
        when (it.itemId) {
            R.id.home_destination -> startActivity(Intent(this, MainActivity::class.java))
            else -> done = NavigationUI.onNavDestinationSelected(it, navController)
        }
        if (done) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        done
    }

}
