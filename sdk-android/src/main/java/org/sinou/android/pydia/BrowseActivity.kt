package org.sinou.android.pydia

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import org.sinou.android.pydia.databinding.ActivityBrowseBinding

class BrowseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_browse)

            setSupportActionBar(binding.toolbar)
            val toggle = ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar, R.string.nav_open,
                R.string.nav_close
            )
            binding.drawerLayout.addDrawerListener(toggle)
            toggle.syncState()

            val navController = findNavController(R.id.browse_fragment_host)
            NavigationUI.setupWithNavController(binding.navView, navController)
            NavigationUI.setupActionBarWithNavController(
                this,
                navController,
                binding.drawerLayout
            )

        } catch (e: Exception) {
            e.printStackTrace()

            e.printStackTrace()

            System.out.println("Here")
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = this.findNavController(R.id.nav_host_fragment)
//        return NavigationUI.navigateUp(navController, drawerLayout)
//    }
}
