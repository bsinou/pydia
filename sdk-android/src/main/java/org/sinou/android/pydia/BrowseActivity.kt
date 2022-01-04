package org.sinou.android.pydia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import org.sinou.android.pydia.browse.ForegroundSessionViewModel
import org.sinou.android.pydia.databinding.ActivityBrowseBinding

class BrowseActivity : AppCompatActivity() {

    private val TAG = "BrowseActivity"

//    private lateinit var sessionVM: ForegroundSessionViewModel

    private lateinit var binding: ActivityBrowseBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_browse)

        var encodedState: String? = null
        if (savedInstanceState != null) {
            // TODO
//            val encodedState: String = savedInstance.getString(GuiNames.EXTRA_STATE)
//            state = State.fromEncodedState(encodedState)
        } else {
            encodedState = intent.getStringExtra(AppNames.EXTRA_STATE)
        }

        val foregroundSessionVMF =
            ForegroundSessionViewModel.ForegroundSessionViewModelFactory(
                CellsApp.instance.accountService,
                CellsApp.instance.nodeService,
                encodedState!!,
                this.application,
            )

//        val tmpVM: ForegroundSessionViewModel by viewModels { foregroundSessionVMF }
//        sessionVM = tmpVM


        buildNavigationLayout()
        binding.navView.setNavigationItemSelectedListener(onMenuItemSelected)

    }

    private fun buildNavigationLayout() {
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar, R.string.nav_open,
            R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navController = findNavController(R.id.browse_fragment_host)
        NavigationUI.setupWithNavController(binding.navView, navController)
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            binding.drawerLayout
        )
    }

    override fun onResume() {
        Log.i(TAG, "onResume, intent: $intent")
        super.onResume()
//         sessionVM.resume()
    }

    override fun onPause() {
        Log.i(TAG, "onPause, intent: $intent")
        super.onPause()
   //      sessionVM.pause()
    }


    val onMenuItemSelected = NavigationView.OnNavigationItemSelectedListener {
        Log.i(TAG, "... Item selected: #${it.itemId}")
        var done = true
        when (it.itemId) {
            R.id.home_destination -> startActivity(Intent(this, MainActivity::class.java))
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
}
