package org.sinou.android.pydia.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainNavDirections
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentChooseWorkspaceBinding
import org.sinou.android.pydia.utils.dumpBackStack

class ChooseWorkspaceFragment : Fragment() {

    companion object {
        private const val fTag = "ChooseWorkspaceFragment"
    }

    private val activeSessionViewModel: ActiveSessionViewModel by activityViewModels()

    private lateinit var binding: FragmentChooseWorkspaceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_choose_workspace, container, false
        )
        return binding.root
    }

    override fun onResume() {
        Log.i(
            fTag, "onResume: ${
                activeSessionViewModel.activeSession.value?.accountID
                    ?: "No active session"
            }"
        )
        dumpBackStack(fTag, parentFragmentManager)

        super.onResume()

        activeSessionViewModel.activeSession.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    val adapter = WorkspaceListAdapter { slug, action -> onWsClicked(slug, action) }
                    binding.workspaces.adapter = adapter
                    val currWss = it.workspaces ?: listOf()
                    adapter.submitList(currWss.sorted())
                }
            },
        )

        CellsApp.instance.getCurrentState()?.let {
            if (it.path != null && it.path.length > 1) {
                Log.i(fTag, "onResume, we have a path: ${it.path}. Navigating")

                val action: NavDirections = when {
                    it.path.startsWith(AppNames.CUSTOM_PATH_BOOKMARKS) -> MainNavDirections.openBookmarks()
                    it.path.startsWith(AppNames.CUSTOM_PATH_ACCOUNTS) -> MainNavDirections.openAccountList()
                    else -> MainNavDirections.openFolder(it.id)
                }
                findNavController().navigate(action)
            }
        }
        activeSessionViewModel.resume()
    }

    private fun onWsClicked(slug: String, command: String) {

        val activeSession = activeSessionViewModel.activeSession.value ?: return
        when (command) {
            BrowseFolderFragment.ACTION_OPEN -> {
                val targetState = StateID.fromId(activeSession.accountID).withPath("/${slug}")
                CellsApp.instance.setCurrentState(targetState)
                findNavController().navigate(MainNavDirections.openFolder(targetState.id))
            }
            else -> return // do nothing
        }
    }

    override fun onPause() {
        Log.i(
            fTag, "Pausing: ${
                activeSessionViewModel.activeSession.value?.accountID
                    ?: "No active session"
            }"
        )
        super.onPause()
        activeSessionViewModel.pause()
    }

//    override fun onSaveInstanceState(savedInstanceState: Bundle) {
//        accountID?.let {
//            savedInstanceState.putSerializable(AppNames.EXTRA_ACCOUNT_ID, it.id)
//        }
//        super.onSaveInstanceState(savedInstanceState)
//    }
}

