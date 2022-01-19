package org.sinou.android.pydia.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.*
import org.sinou.android.pydia.databinding.FragmentChooseWorkspaceBinding

class ChooseWorkspaceFragment : Fragment() {

    private val fTag = "ChooseWorkspaceFragment"

    private val activeSessionViewModel: ActiveSessionViewModel by activityViewModels()

    private lateinit var binding: FragmentChooseWorkspaceBinding
    private var accountID: StateID? = null

//    private var targetState: StateID? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        when {
//            savedInstanceState?.getString(AppNames.EXTRA_ACCOUNT_ID) != null -> {
//                accountID =
//                    StateID.fromId(savedInstanceState.getString(AppNames.EXTRA_ACCOUNT_ID))!!
//            }
//            requireActivity().intent != null -> {
//                requireActivity().intent.getStringExtra(AppNames.EXTRA_STATE)?.let {
//                    targetState = StateID.fromId(it)
//                    accountID = StateID.fromId(targetState!!.accountId)
//                }
//            }
//        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_choose_workspace, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = WorkspaceListAdapter { slug, action -> onWsClicked(slug, action) }
        binding.workspaces.adapter = adapter

        activeSessionViewModel.activeSession.observe(
            viewLifecycleOwner,
            { liveSession ->
                liveSession?.let {
                    val currWss = it.workspaces ?: listOf()
                    adapter.submitList(currWss.sorted())
                }
            },
        )
    }

    private fun onWsClicked(slug: String, command: String) {

        val activeSession = activeSessionViewModel.activeSession.value ?: return
        when (command) {
            BrowseFolderFragment.ACTION_OPEN -> {
                val targetState = StateID.fromId(activeSession.accountID).withPath("/${slug}")
                findNavController().navigate(MainNavDirections.openFolder(targetState.id))
            }
            else -> return // do nothing
        }
    }

    override fun onResume() {
        Log.i(fTag, "onResume: $accountID")
        super.onResume()


        CellsApp.instance.lastState()?.let {
            if (it.path != null && it.path.length > 1) {
                Log.i(fTag, "onResume, we have a path: ${it.path}. Navigating")
                val action = MainNavDirections.openFolder(it.id)
                findNavController().navigate(action)
            }
        }

        activeSessionViewModel.resume()
    }

    override fun onPause() {
        Log.i(fTag, "Pausing: $accountID")
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

