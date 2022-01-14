package org.sinou.android.pydia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import org.sinou.android.pydia.databinding.ActivityBrowseBinding

class BrowseActivity : AppCompatActivity() {

    companion object {
        private const val tag = "BrowseActivity"
        const val actionNavigate = "navigate"
        const val actionMore = "more"
    }

    private lateinit var binding: ActivityBrowseBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_browse)
        buildNavigationLayout()

        // Rather done in the workspace fragment that is the start point for the Browse activity.
//        var encodedState: String? = if (savedInstanceState != null) {
//            savedInstanceState.getString(AppNames.EXTRA_STATE)
//        } else {
//            intent.getStringExtra(AppNames.EXTRA_STATE)
//        }
//
//        if (Str.notEmpty(encodedState)){
//            val stateID = StateID.fromId(encodedState)
//            if (stateID.file.length > 1){
//
//            }
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

        navController = this.findNavController(R.id.browse_fragment_host)
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            binding.drawerLayout
        )

        binding.navView.setNavigationItemSelectedListener(onMenuItemSelected)
    }

    override fun onBackPressed() {
        val drawer = binding.drawerLayout
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

    private val onMenuItemSelected = NavigationView.OnNavigationItemSelectedListener {
        Log.i(tag, "... Item selected: #${it.itemId}")
        var done = true
        when (it.itemId) {
            R.id.home_destination -> startActivity(Intent(this, HomeActivity::class.java))
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

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, binding.drawerLayout)
    }

}
