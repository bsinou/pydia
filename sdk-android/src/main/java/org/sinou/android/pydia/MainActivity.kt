package org.sinou.android.pydia

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.browse.ActiveSessionViewModel
import org.sinou.android.pydia.browse.getIconForWorkspace
import org.sinou.android.pydia.databinding.ActivityMainBinding
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    companion object {
        private const val tag = "MainActivity"
    }

    private val activeSessionVM: ActiveSessionViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val encodedState: String? = if (savedInstanceState != null) {
//            savedInstanceState.getString(AppNames.EXTRA_STATE)
//        } else {
//            intent.getStringExtra(AppNames.EXTRA_STATE)
//        }

        // We initialise the nav controller early to enable navigation to the first page,
        // even if no other binding is defined.
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navController = findNavController(R.id.main_fragment_host)

        buildNavigationLayout()

//        if (Str.notEmpty(encodedState)) {
//            sessionVM.setActiveAccount(StateID.fromId(StateID.fromId(encodedState).accountId))
//        } else {
//            // If we arrive here, it means that we have no state recorded
//            // but more than one account defined
//            supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.main_fragment_host, AccountListFragment())
//                .commit()
//        }
//

//        if (activeSessionVM.activeSession.value == null) {
//            // If we arrive here, it means that we have no foreground session
//            // and more than one account defined
//            supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.main_fragment_host, AccountListFragment())
//                .commit()
//
//        }

    }

    private fun buildNavigationLayout() {
        setSupportActionBar(binding.toolbar)

        // This can be used if we want to always trigger drawer opening
        // while clicking on the top left icon. See also BrowseFolderFragment
//        val toggle = ActionBarDrawerToggle(
//            this, binding.drawerLayout, binding.toolbar, R.string.nav_open,
//            R.string.nav_close
//        )
//        binding.drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()
//        toggle.setToolbarNavigationClickListener { onBackPressed() }

        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            binding.mainDrawerLayout
        )

        binding.navView.setNavigationItemSelectedListener(onMenuItemSelected)

        // Configure listeners for Navigation header... Might be improved.
        val header = binding.navView.getHeaderView(0)
        val switchBtn = header.findViewById<ImageButton>(R.id.nav_header_switch_account)
        switchBtn?.setOnClickListener {
            navController.navigate(MainNavDirections.openAccountList())
            closeDrawer()
        }
        val btn = header.findViewById<ImageButton>(R.id.nav_header_exit)
        btn?.setOnClickListener {
            closeDrawer()
            finish()
            exitProcess(0)
        }
        wireNavViewObserver()
    }

    private fun closeDrawer() {
        binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
    }

    private val onMenuItemSelected = NavigationView.OnNavigationItemSelectedListener {
        Log.i(tag, "... Item selected: #${it.itemId}")
        var done = true
        when (it.itemId) {
            else -> done = NavigationUI.onNavDestinationSelected(it, navController)
        }
        if (done) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }
        done
    }

    private fun wireNavViewObserver() {

//        if (activeSessionVM.activeSession.value == null) {
//            return
//        }

        val item = binding.navView.menu.findItem(R.id.ws_section)
        activeSessionVM.activeSession.observe(
            this,
            {
                it?.let { liveSession ->
                    item.subMenu.clear()
                    for (ws in liveSession.workspaces?.sorted() ?: listOf()) {
                        val wsItem = item.subMenu.add(ws.label)
                        wsItem.icon = ContextCompat.getDrawable(this, getIconForWorkspace(ws))
                        wsItem.setOnMenuItemClickListener {
                            val state = StateID.fromId(liveSession.accountID)
                                .withPath("/${ws.slug}")
                            navController.navigate(MainNavDirections.openFolder(state.id))
                            closeDrawer()
                            true
                        }
                    }

                    // Also update meta info in the header
                    val header = binding.navView.getHeaderView(0)
                    val primaryText = header.findViewById<TextView>(R.id.nav_header_primary_text)
                    primaryText.text = liveSession.username
                    val secondaryText = header.findViewById<TextView>(R.id.nav_header_secondary_text)
                    secondaryText.text = liveSession.url
                }
            },
        )
        binding.navView.invalidate()
    }

    override fun onBackPressed() {
        val drawer = binding.mainDrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        Log.d(tag, "onResume, intent: $intent")
        super.onResume()
    }

    override fun onPause() {
        Log.d(tag, "onPause, intent: $intent")
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, binding.mainDrawerLayout)
    }
}
