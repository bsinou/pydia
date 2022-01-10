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
import org.sinou.android.pydia.room.account.RAccount

/**
 * Manage default pages of the app.
 */
class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private var nextPage: Intent? = null


    companion object {
        const val logoCrossFadeDurationMillis = 300
        const val spacingAfterFadeDurationMillis = 150
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Compute next destination
        chooseFirstPage()

        fadeOut(savedInstanceState)
    }

    // Called when ready
    private fun doInflate() {
        if (nextPage == null) {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
            buildNavigationLayout()
            binding.navView.setNavigationItemSelectedListener(onMenuItemSelected)
        } else {
            startActivity(nextPage)
        }
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
            val accounts = CellsApp.instance.accountService.accountDB.accountDao().getAccounts()
         //   lifecycleScope.launch {
                val size = accounts?.size ?: -1
                nextPage = when {
                    size == 0 -> Intent(act, AuthActivity::class.java)
                    size == 1 -> {
                        val tmp = Intent(act, BrowseActivity::class.java)
                        tmp.putExtra(
                            AppNames.EXTRA_STATE,
                            (accounts as List<RAccount>)[0].accountID
                        )
                        tmp
                    }
                    size > 1 -> Intent(act, AccountActivity::class.java)
                    else -> null
                }
           // }
        }

//        var accounts: List<RAccount>? = null
//        val launch = CoroutineScope(Dispatchers.Default).launch {
//            accounts = CellsApp.instance.accountService.accountDB.accountDao().getAccounts()
//
//            val size = accounts?.size ?: -1
//            nextPage = when {
//                size == 0 -> Intent(this, AuthActivity::class.java)
//                size == 1 -> {
//                    val tmp = Intent(this, BrowseActivity::class.java)
//                    tmp.putExtra(AppNames.EXTRA_STATE, (accounts as List<RAccount>)[0].accountID)
//                    tmp
//                }
//                size > 1 -> Intent(this, AccountActivity::class.java)
//                else -> null
//            }
//        }

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
