package org.sinou.android.pydia

import android.app.ActivityManager
import android.net.TrafficStats
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import org.sinou.android.pydia.databinding.ActivityMainBinding
import org.sinou.android.pydia.ui.bindings.getWsIconForMenu
import org.sinou.android.pydia.ui.browse.ActiveSessionViewModel
import org.sinou.android.pydia.ui.home.clearCache
import org.sinou.android.pydia.ui.search.SearchFragment
import org.sinou.android.pydia.utils.dumpBackStack
import org.sinou.android.pydia.utils.showMessage
import java.util.*
import kotlin.system.exitProcess

/**
 * Central activity for browsing, managing accounts and settings. Various
 * screens are implemented via fragments that are gathered in the ui package.
 * */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val tag = "MainActivity"
    }

    private lateinit var activeSessionVM: ActiveSessionViewModel
    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(CellsApp.instance.currentTheme)
        super.onCreate(savedInstanceState)

        var encodedState = savedInstanceState?.getString(AppNames.KEY_STATE)
        if (Str.empty(encodedState)) {
            encodedState = intent.getStringExtra(AppNames.EXTRA_STATE)
        }

        val accountState = encodedState?.let { StateID.fromId(it).accountId }

        Log.d(tag, "onCreate for: ${StateID.fromId(encodedState)}")

        val viewModelFactory = ActiveSessionViewModel.ActiveSessionViewModelFactory(
            CellsApp.instance.accountService,
            accountState,
            application,
        )
        val tmpVM: ActiveSessionViewModel by viewModels { viewModelFactory }
        activeSessionVM = tmpVM

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.mainToolbar)

        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.main_fragment_host)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.mainDrawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        // Add custom listeners
        binding.navView.setNavigationItemSelectedListener(onMenuItemSelected)

        configureObservers()

// TODO back navigation is still clumsy, the "onBackPress() method from activity
        //   and thus the custom adapters are not called when back is triggered by clicking
        //   the app bar arrow...
//        binding.toolbar.setNavigationOnClickListener {
//            onBackPressed()
//        }

//        NetworkStatusHelper(this@MainActivity).observe(this, {
//            showMessage(
//                this@MainActivity,
//                when (it) {
//                    NetworkStatus.Available -> "Network Connection Established"
//                    NetworkStatus.Unavailable -> "No Internet"
//                }
//            )
//        })
        handleStateOrIntent(savedInstanceState)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        activeSessionVM.accountId?.let {
            Log.e(tag, "onSaveInstanceState for: ${StateID.fromId(it)}")
            outState.putString(AppNames.KEY_STATE, it)
        }
    }

    private fun networkUsage() {
        // Get running processes
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningApps = manager.runningAppProcesses
        for (runningApp in runningApps) {
            val received = TrafficStats.getUidRxBytes(runningApp.uid)
            val sent = TrafficStats.getUidTxBytes(runningApp.uid)
            Log.d(
                tag, java.lang.String.format(
                    Locale.getDefault(),
                    "uid: %1d - name: %s: Sent = %1d, Received = %1d",
                    runningApp.uid,
                    runningApp.processName,
                    sent,
                    received
                )
            )
        }
    }

    private fun handleStateOrIntent(savedInstanceState: Bundle?) {

        var stateId = savedInstanceState?.getString(AppNames.KEY_STATE)
        if (Str.empty(stateId)) {
            stateId = intent.getStringExtra(AppNames.EXTRA_STATE)
        }
        if (Str.empty(stateId)) {
            navController.navigate(MainNavDirections.openAccountList())
            return
        }
        StateID.fromId(stateId)?.let {
            when {
                Str.notEmpty(it.workspace) -> {
                    val action = MainNavDirections.openFolder(it.id)
                    navController.navigate(action)
                }
                // this is the default
//                it.path == null -> {
//                    val action = MainNavDirections.openWorkspaces(it.id)
//                    navController.navigate(action)
//                }

            }
        }
    }

    private fun closeDrawer() {
        binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
    }

    private val onMenuItemSelected = NavigationView.OnNavigationItemSelectedListener {
        Log.i(tag, "... Item selected: #${it.itemId}")
        var done = false
        when (it.itemId) {
            R.id.open_bookmarks -> {
                activeSessionVM.liveSession.value?.let { session ->
                    val target = StateID.fromId(session.accountID)
                        .child(AppNames.CUSTOM_PATH_BOOKMARKS)
                    CellsApp.instance.setCurrentState(target)
                    navController.navigate(MainNavDirections.openBookmarks())
                    done = true
                }
            }
            R.id.clear_cache -> {
                activeSessionVM.liveSession.value?.let { session ->
                    clearCache(binding.root.context, session.accountID)
                    done = true
                }
            }
            else -> done = NavigationUI.onNavDestinationSelected(it, navController)
        }
        if (done) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }
        done
    }

    private fun configureObservers() {

        // Configure navigation View header buttons
        val header = binding.navView.getHeaderView(0)
        val switchBtn = header.findViewById<ImageButton>(R.id.nav_header_switch_account)
        switchBtn?.setOnClickListener {
            navController.navigate(MainNavDirections.openAccountList())
            closeDrawer()
        }
        val exitBtn = header.findViewById<ImageButton>(R.id.nav_header_exit)
        exitBtn?.setOnClickListener {
            closeDrawer()
            finish()
            exitProcess(0)
        }

        if (activeSessionVM.accountId == null) {
            return
        }
        val accId = activeSessionVM.accountId

        // Observe current live session to update the UI
        activeSessionVM.liveSession.observe(this) {
            it?.let { liveSession ->

                // Change base them based on current session status
                val newTheme = when (it.authStatus) {
                    AppNames.AUTH_STATUS_CONNECTED -> R.style.Theme_Cells
                    else ->  R.style.Theme_Cells_Offline
                }
                if (newTheme != CellsApp.instance.currentTheme) {
                    CellsApp.instance.currentTheme = newTheme
                    recreate()
                }

                // Set current session info in the Navigation view header
                val headerView = binding.navView.getHeaderView(0)
                val primaryText =
                    headerView.findViewById<TextView>(R.id.nav_header_primary_text)
                primaryText.text = liveSession.username
                val secondaryText =
                    headerView.findViewById<TextView>(R.id.nav_header_secondary_text)
                secondaryText.text = liveSession.url

                // Force refresh of the navigation view
                binding.navView.invalidate()
            }
        }

        // Workspaces
        val wsMenuSection = binding.navView.menu.findItem(R.id.ws_section)
        activeSessionVM.workspaces.observe(this) {
            if (it.isNotEmpty()) {
                wsMenuSection.subMenu.clear()
                for (ws in it) {
                    val wsItem = wsMenuSection.subMenu.add(ws.label)
                    wsItem.icon = ContextCompat.getDrawable(this, getWsIconForMenu(ws))
                    wsItem.setOnMenuItemClickListener {
                        val state = StateID.fromId(accId).withPath("/${ws.slug}")
                        CellsApp.instance.setCurrentState(state)
                        navController.navigate(MainNavDirections.openFolder(state.id))
                        closeDrawer()
                        true
                    }
                }
                binding.navView.invalidate()
            }
        }

    }

    override fun onStart() {
        Log.d(tag, "onStart, intent: $intent")
        super.onStart()
    }

    override fun onStop() {
        Log.d(tag, "onStop, intent: $intent")
        super.onStop()
    }

    override fun onResume() {
        Log.d(tag, "onResume, intent: $intent")
        Log.d(tag, "#### Calling network usage for: ${activeSessionVM.accountId}")
        networkUsage()
        super.onResume()
        dumpBackStack(tag, supportFragmentManager)
    }

    override fun onPause() {
        Log.d(tag, "onPause, intent: $intent")
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_fragment_host)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val drawer = binding.mainDrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_options, menu)

        val searchItem = menu.findItem(R.id.search_edit_view)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(SearchListener())
        }

        configureLayoutSwitcher(menu)

        return true
    }

    private fun configureLayoutSwitcher(menu: Menu) {
        val layoutSwitcher = menu.findItem(R.id.switch_recycler_layout)

        val showSwitch = navController.currentDestination?.let {
            when (it.id) {
                R.id.search_destination -> true
                R.id.bookmark_list_destination -> true
                R.id.browse_folder_destination -> true
                R.id.offline_root_list_destination -> true
                else -> false
            }
        } ?: false

        layoutSwitcher.isVisible = showSwitch
        if (!showSwitch) {
            return
        }

        val oldValue = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_LAYOUT)
        val storedLayout = oldValue ?: AppNames.RECYCLER_LAYOUT_LIST

        layoutSwitcher.icon = when (storedLayout) {
            AppNames.RECYCLER_LAYOUT_GRID ->
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_view_list_24,
                    theme
                )
            else ->
                ResourcesCompat.getDrawable(resources, R.drawable.ic_sharp_grid_view_24, theme)
        }

        layoutSwitcher.setOnMenuItemClickListener {
            val value = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_LAYOUT)
            val newValue = if (value != null && AppNames.RECYCLER_LAYOUT_GRID == value) {
                AppNames.RECYCLER_LAYOUT_LIST
            } else {
                AppNames.RECYCLER_LAYOUT_GRID
            }
            CellsApp.instance.setPreference(AppNames.PREF_KEY_CURR_RECYCLER_LAYOUT, newValue)

            this.recreate()

            return@setOnMenuItemClickListener true
        }
    }

    private inner class SearchListener : OnQueryTextListener {

        // FIXME clean this class:
        //   - why local state?
        //   - externalize

        private var searchFragment: SearchFragment? = null
        private var stateId: StateID? = null
        private var uiContext: String? = null

        override fun onQueryTextChange(newText: String): Boolean {
            if (Str.empty(newText)) return true
            // TODO for the time being, we do not remote query at each key stroke.
//            navController.currentDestination?.let {
//                if (it.id == R.id.search_destination) {
//                    getSearchFragment()?.updateQuery(newText)
//                }
//            }
            return true
        }

        override fun onQueryTextSubmit(query: String): Boolean {
            navController.currentDestination?.let {
                if (it.id == R.id.search_destination) {
                    getSearchFragment()?.updateQuery(query)
                } else {
                    retrieveCurrentContext()
                    stateId?.let { state ->
                        val action =
                            MainNavDirections.searchEditView(state.id, uiContext!!, query)
                        navController.navigate(action)
                    }
                }
            }
            return true
        }

        private fun retrieveCurrentContext() {
            if (activeSessionVM.liveSession.value == null) {
                showMessage(baseContext, "Cannot search with no active session")
                return
            }
            // showMessage(baseContext, "About to navigate")

            stateId = StateID.fromId(activeSessionVM.liveSession.value!!.accountID)
            uiContext = when (navController.currentDestination!!.id) {
                R.id.bookmark_list_destination -> AppNames.CUSTOM_PATH_BOOKMARKS
                else -> ""
            }
        }

        private fun getSearchFragment(): SearchFragment? {
            searchFragment?.let { return it }
            supportFragmentManager.findFragmentById(R.id.main_fragment_host)
                ?.childFragmentManager?.findFragmentById(R.id.main_fragment_host)?.let {
                    searchFragment = it as SearchFragment
                }
            return searchFragment
        }
    }
}
