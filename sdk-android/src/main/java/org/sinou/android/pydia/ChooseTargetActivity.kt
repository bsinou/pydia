package org.sinou.android.pydia

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.sinou.android.pydia.databinding.ActivityChooseTargetBinding
import org.sinou.android.pydia.ui.upload.ChooseTargetViewModel
import org.sinou.android.pydia.utils.showMessage

/**
 * Let the end-user choose a target in one of the defined remote servers.
 */
class ChooseTargetActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val tag = ChooseTargetActivity::class.simpleName

    private lateinit var binding: ActivityChooseTargetBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    private lateinit var model: ChooseTargetViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag, "onCreate: launching target choice process")
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_choose_target)
        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.upload_fragment_host)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val chooseTargetFactory = ChooseTargetViewModel.ChooseTargetViewModelFactory(
            CellsApp.instance.transferService,
            application,
        )
        val tmpVM: ChooseTargetViewModel by viewModels { chooseTargetFactory }
        model = tmpVM

        tmpVM.currentLocation.observe(this) {
            binding.toolbar.menu.findItem(R.id.launch_upload)?.let {
                if (it.isVisible != tmpVM.isTargetValid()) {
                    it.isVisible = !it.isVisible
                }
                binding.executePendingBindings()
            }
        }

        tmpVM.postDone.observe(this) {
            if (it) {
                showMessage(this, "And returning")
                finishAndRemoveTask()
            }
        }

        tmpVM.postIntent.observe(this) {
            it?.let {
                Log.d(tag, "Result OK, target state: ${tmpVM.currentLocation.value}")
                setResult(Activity.RESULT_OK, it)
                finishAndRemoveTask()
            }
        }

        // supportActionBar?.title = "My Title"
        // Log.e(tag, "Got a **support** action bar: $supportActionBar")
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onResume() {
        Log.i(tag, "onResume, intent: $intent")
        super.onResume()
        handleIntent(intent)
        // supportActionBar?.title = "My Title From on resume"
    }

    override fun onPause() {
        Log.i(tag, "onPause, intent: $intent")
        super.onPause()
    }

    private fun handleIntent(inIntent: Intent) {

        when (inIntent.action) {
            AppNames.ACTION_CHOOSE_TARGET -> {
                val actionContext =
                    intent.getStringExtra(AppNames.EXTRA_ACTION_CONTEXT) ?: AppNames.ACTION_COPY
                model.setActionContext(actionContext)
                val stateID = StateID.fromId(intent.getStringExtra(AppNames.EXTRA_STATE))
                model.setCurrentState(stateID)
            }
            Intent.ACTION_SEND -> {
                val clipData = intent.clipData
                clipData?.let {
                    model.setActionContext(AppNames.ACTION_UPLOAD)
                    model.initUploadAction(listOf(clipData.getItemAt(0).uri))
                }
                // TODO retrieve starting state from: ?
                // CellsApp.instance.getCurrentState()
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val clipData = intent.clipData
                clipData?.let {
                    // Here also ?
                    model.setActionContext(AppNames.ACTION_UPLOAD)
                    val uris = mutableListOf<Uri>()
                    for (i in 0 until it.itemCount) {
                        uris.add(clipData.getItemAt(i).uri)
                    }
                    model.initUploadAction(uris)
                }
            }
        }

        // Directly go inside a target location if defined
        model.currentLocation.value?.let {
            val action = UploadNavigationDirections.actionPickFolder(it.id)
            navController.navigate(action)
            return
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.upload_options, menu)
        return true
    }

    override fun onStop() {
        Log.d(tag, "onStop: target state: ${model.currentLocation.value}")
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {

            R.id.launch_upload -> {
                model.launchPost(this)
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
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
