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
import org.sinou.android.pydia.databinding.ActivityHomeBinding

/**
 * Root activity
 */
class HomeActivity : AppCompatActivity() {

    private val tag = "HomeActivity"

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildNavigationLayout()
    }

    private fun buildNavigationLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.home_fragment_host)
        NavigationUI.setupWithNavController(binding.navView, navController)
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            binding.drawerLayout
        )

        binding.navView.setNavigationItemSelectedListener(onMenuItemSelected)

    }

    private val onMenuItemSelected = NavigationView.OnNavigationItemSelectedListener {
        Log.i(tag, "... Item selected: #${it.itemId}")

        var done = true
        when (it.itemId) {
            R.id.accounts_destination -> {
                // binding.drawerLayout.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, AccountActivity::class.java))
            }
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

    override fun onPause() {
        Log.d(tag, "onPause")
        super.onPause()
    }

    override fun onResume() {
        Log.d(tag, "onResume")
        super.onResume()
    }

}
