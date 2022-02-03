package org.sinou.android.pydia.ui.browse

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
import org.sinou.android.pydia.databinding.FragmentWorkspaceListBinding

class ChooseWorkspaceFragment : Fragment() {

    companion object {
        private const val fTag = "ChooseWorkspaceFragment"
    }

    private val activeSessionVM: ActiveSessionViewModel by activityViewModels()
    private lateinit var binding: FragmentWorkspaceListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_workspace_list, container, false
        )
        return binding.root
    }

    override fun onResume() {
        Log.i(
            fTag, "onResume: ${
                activeSessionVM.activeSession.value?.accountID
                    ?: "No active session"
            }"
        )
        super.onResume()

        activeSessionVM.activeSession.observe(viewLifecycleOwner) {
            it?.let {
                val adapter = WorkspaceListAdapter { slug, action -> onWsClicked(slug, action) }
                binding.workspaces.adapter = adapter
                val currWss = it.workspaces
                if (currWss == null || currWss.isEmpty()) {
                    binding.emptyContent.visibility = View.VISIBLE
                    binding.workspaces.visibility = View.GONE
                } else {
                    binding.workspaces.visibility = View.VISIBLE
                    binding.emptyContent.visibility = View.GONE
                    adapter.submitList(currWss.sorted())
                }
            }
        }

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
        activeSessionVM.resume()
    }

    private fun onWsClicked(slug: String, command: String) {
        val activeSession = activeSessionVM.activeSession.value ?: return
        when (command) {
            AppNames.ACTION_OPEN -> {
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
                activeSessionVM.activeSession.value?.accountID
                    ?: "No active session"
            }"
        )
        super.onPause()
        activeSessionVM.pause()
    }

}

