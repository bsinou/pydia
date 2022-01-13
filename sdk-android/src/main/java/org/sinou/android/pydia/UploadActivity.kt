package org.sinou.android.pydia

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sinou.android.pydia.auth.ServerUrlFragmentDirections
import org.sinou.android.pydia.databinding.ActivityUploadBinding
import org.sinou.android.pydia.upload.ChooseTargetViewModel

/**
 * Receives files from other apps.
 */
class UploadActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val tag = "UploadActivity"

    private lateinit var binding: ActivityUploadBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var model: ChooseTargetViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.upload_fragment_host)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val chooseTargetFactory = ChooseTargetViewModel.ChooseTargetViewModelFactory(
            CellsApp.instance.nodeService,
            application,
        )
        val tmpVM: ChooseTargetViewModel by viewModels { chooseTargetFactory }
        model = tmpVM

        tmpVM.currentLocation.observe(this, {
            binding.toolbar.menu.findItem(R.id.launch_upload)?.isVisible = model.validTarget()
        })
    }

    override fun onResume() {
        Log.i(tag, "onResume, intent: $intent")
        super.onResume()
        handleIntent(intent)
    }

    override fun onPause() {
        Log.i(tag, "onPause, intent: $intent")
        super.onPause()
    }

    private fun handleIntent(inIntent: Intent) {

        when (inIntent.action){
            Intent.ACTION_SEND -> {
                val clipData = intent.clipData

                clipData?.let{
                    model.initTarget(listOf(clipData.getItemAt(0).uri))

                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val clipData = intent.clipData

                clipData?.let{
                    val uris = mutableListOf<Uri>()
                    for (i in 0 until it.itemCount){
                        uris.add(clipData.getItemAt(i).uri)
                    }
                    model.initTarget(uris)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.upload_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {

            R.id.launch_upload -> {
                model.launchUpload()
                Toast.makeText(this, "Launching upload in background", Toast.LENGTH_SHORT).show()

                val act = this
                lifecycleScope.launch {
                    // Time between the cross fade and start screen animation
                    delay(500L)
                    Toast.makeText(act, "And returning", Toast.LENGTH_SHORT).show()
                    act.finishAndRemoveTask()
                }
                true
            }

            R.id.cancel_upload -> {
                this.finishAndRemoveTask()
                true
            }
            // R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.upload_fragment_host)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
