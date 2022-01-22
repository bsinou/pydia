package org.sinou.android.pydia

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
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.databinding.ActivityMainBinding
import org.sinou.android.pydia.ui.browse.ActiveSessionViewModel
import org.sinou.android.pydia.ui.browse.getIconForWorkspace
import org.sinou.android.pydia.utils.dumpBackStack
import kotlin.system.exitProcess


/**
 * Central activity for browsing, managing accounts and settings. Various
 * screens are implemented via fragments that are gathered in the ui package.
 * */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val tag = "MainActivity"
    }

    private val activeSessionVM: ActiveSessionViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.mainToolbar)

        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.main_fragment_host)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.mainDrawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        // Add custom listeners
        binding.navView.setNavigationItemSelectedListener(onMenuItemSelected)

        wireNavViewObserver()

// TODO back navigation is still clumsy, the "onBackPress() method from activity
        //   and thus the custom adapters are not called when back is triggered by clicking
        //   the app bar arrow...
//        binding.toolbar.setNavigationOnClickListener {
//            onBackPressed()
//        }

    }

    private fun closeDrawer() {
        binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
    }

    private val onMenuItemSelected = NavigationView.OnNavigationItemSelectedListener {
        Log.i(tag, "... Item selected: #${it.itemId}")
        var done = false
        when (it.itemId) {
            R.id.open_bookmarks -> {
                activeSessionVM.activeSession.value?.let { session ->
                    val target = StateID.fromId(session.accountID)
                        .child(AppNames.CUSTOM_PATH_BOOKMARKS)
                    CellsApp.instance.setCurrentState(target)
                    navController.navigate(MainNavDirections.openBookmarks())
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

    private fun wireNavViewObserver() {

        // Top header button to switch account and logout
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

        // Workspaces
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
                            CellsApp.instance.setCurrentState(state)
                            navController.navigate(MainNavDirections.openFolder(state.id))
                            closeDrawer()
                            true
                        }
                    }

                    // Also update meta info in the header
                    val headerView = binding.navView.getHeaderView(0)
                    val primaryText =
                        headerView.findViewById<TextView>(R.id.nav_header_primary_text)
                    primaryText.text = liveSession.username
                    val secondaryText =
                        headerView.findViewById<TextView>(R.id.nav_header_secondary_text)
                    secondaryText.text = liveSession.url
                }
            },
        )

        // search
//         val item = binding.navView.menu.findItem(R.id.ws_section)


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
        dumpBackStack(tag, supportFragmentManager)
    }

    override fun onPause() {
        Log.d(tag, "onPause, intent: $intent")
        super.onPause()
    }

    // TODO this is not good-enough:
    // We do not want to explicitely cast the activity in the fragment...
    private var searchView: SearchView? = null
    private var queryTextListener: OnQueryTextListener? = null

    private var currFragmentStateID: StateID? = null
    private var currFragmentContext: String? = null

    fun registerForSearch(stateId: StateID?, uiContext: String?){
        currFragmentStateID = stateId
        currFragmentContext = uiContext
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_options, menu)

        Log.i("### option menu", "$currFragmentStateID")

        val searchItem = menu.findItem(R.id.search)

        if (searchItem != null) {
            searchView = searchItem.getActionView() as SearchView
        }
//        if (searchView != null) {
//            searchView.setSearchableInfo(
//                this.getSystemService(Context.SEARCH_SERVICE)
//                    .getSearchableInfo(this.getComponentName())
//            )
//        }

        val queryTextListener: OnQueryTextListener = object : OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                Log.i("onQueryTextChange", newText)
                val currentFragment = findNavController(R.id.main_fragment_host)?.currentDestination
                currentFragment?.let{
                    Log.i("onQueryTextSubmit", "Retrieving query from ${it.arguments["state"].toString()}")
                }
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                currFragmentStateID?.let{
                    val action = MainNavDirections.search(it.id, currFragmentContext!!, query)
                    navController.navigate(action)
                }
                Log.i("onQueryTextSubmit", query)
                return true
            }
        }
        searchView?.setOnQueryTextListener(queryTextListener)

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_fragment_host)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
