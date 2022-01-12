package org.sinou.android.pydia

import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sinou.android.pydia.databinding.ActivityMainBinding

/**
 * Manage default pages of the app.
 */
class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private var nextPage: Intent? = null

    companion object {
        const val tickDuration = 1000L
        const val logoCrossFadeDurationMillis = 300
        const val spacingAfterFadeDurationMillis = 150
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Compute next destination
        if (intent.categories.contains(Intent.CATEGORY_LAUNCHER)) {
            chooseFirstPage()
            fadeOut(savedInstanceState)
//        } else {
//            doInflate()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(tag, "onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.i(tag, "onResume")

        if (nextPage != null) {
            startActivity(nextPage)
        }
    }

    private suspend fun waitForIt() {
        repeat(10) { // we wait at most ten seconds before crashing
            Log.i(tag, "Waiting for backend to be ready")
            if (CellsApp.instance.ready) {
                Log.i(tag, "### Backend is now ready")
                return
            }
            delay(tickDuration)
        }
    }

    // Called when ready
    private fun doInflate() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        buildNavigationLayout()
        binding.navView.setNavigationItemSelectedListener(onMenuItemSelected)
    }

    // Thanks to https://www.tiagoloureiro.tech/posts/definitive-guide-for-splash-screen-android/
    private fun fadeOut(savedInstanceState: Bundle?) {
        // Small trick: we check if we have a saved bundle to avoid showing the splash twice
        val alreadyShown = savedInstanceState != null
        if (!alreadyShown) {
            (window.decorView.background as TransitionDrawable).startTransition(
                logoCrossFadeDurationMillis
            )
            // Use a coroutine to block during animation, then sets the view
            lifecycleScope.launch {
                // Time between the cross fade and start screen animation
                delay(logoCrossFadeDurationMillis.toLong() + spacingAfterFadeDurationMillis)
                window.decorView.background = AppCompatResources.getDrawable(
                    this@MainActivity, R.drawable.background
                )
                doInflate()
                //setContentView(R.layout.activity_main)
            }
        } else {
            // Splash was shown before, no need to animate the transition.
            // 1 - Sets the window background to the background without the logo (if needed)
            window.decorView.background = AppCompatResources.getDrawable(
                this, R.drawable.background
            )
            // 2 - Sets the content view instantly
            doInflate()
            //  setContentView(R.layout.activity_main)
        }
    }

    private fun chooseFirstPage() {
        val act = this
        CoroutineScope(Dispatchers.Default).launch {
            waitForIt()

            Log.i(tag, "### Backend is ready, about to choose first page")

            // Try to restart from where we left it
            CellsApp.instance.lastState()?.let {
                val tmp = Intent(act, BrowseActivity::class.java)
                tmp.putExtra(AppNames.EXTRA_STATE, it.id)
                nextPage = tmp
                return@launch
            }

            // Choose between new account or account list when we have no state.
            // We go to workspace list when we have only one account
            val accounts = CellsApp.instance.accountService.accountDB.accountDao().getAccounts()
            val size = accounts.size
            nextPage = when {
                size == 0 -> Intent(act, AuthActivity::class.java)
                size == 1 -> {
                    val tmp = Intent(act, BrowseActivity::class.java)
                    tmp.putExtra(AppNames.EXTRA_STATE, accounts[0].accountID)
                    tmp
                }
                size > 1 -> Intent(act, AccountActivity::class.java)
                else -> null
            }
        }

        // TODO also set-up worker tasks
        Log.i(tag, "Delayed init terminated")


//        if (Intent.ACTION_VIEW == intent.action) {
//            val uri = inIntent.data ?: return
//            val code = uri.getQueryParameter(AppNames.KEY_CODE)
//            val state  = uri.getQueryParameter(AppNames.KEY_STATE)
//
//            if (code != null && state != null){
//                val action = ServerUrlFragmentDirections.actionServerUrlToOauthFlow(null)
//                findNavController(R.id.auth_fragment_host).navigate(action)
//            }
//
//            launch {
//                // CellsApp.instance.accountService.handleOAuthResponse(state, code)
//            }
//        }
    }


    private fun buildNavigationLayout() {
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar, R.string.nav_open,
            R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navController = findNavController(R.id.main_fragment_host)
        NavigationUI.setupWithNavController(binding.navView, navController)
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            binding.drawerLayout
        )
    }

    private val onMenuItemSelected = NavigationView.OnNavigationItemSelectedListener {
        Log.i(tag, "... Item selected: #${it.itemId}")

        var done = true
        when (it.itemId) {
            R.id.account_list_destination -> startActivity(
                Intent(
                    this,
                    AccountActivity::class.java
                )
            )
            else -> done = NavigationUI.onNavDestinationSelected(it, navController)
        }
        if (done) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        done
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = this.findNavController(R.id.nav_host_fragment)
//        return NavigationUI.navigateUp(navController, drawerLayout)
//    }

/*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG,"###### Options Item selected: " + item.itemId)
        return when (item.itemId) {
            //  Otherwise, do nothing and use the core event handling
            else -> super.onOptionsItemSelected(item)
        }
    }
*/

}
