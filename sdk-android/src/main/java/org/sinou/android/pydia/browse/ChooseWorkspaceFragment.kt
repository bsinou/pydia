package org.sinou.android.pydia.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.BrowseActivity
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentChooseWorkspaceBinding

class ChooseWorkspaceFragment : Fragment() {

    private val fTag = "ChooseWorkspaceFragment"

    private lateinit var accountID: String

    private var targetState: StateID? = null

    private lateinit var binding: FragmentChooseWorkspaceBinding

    // private lateinit var sessionVM: ForegroundSessionViewModel
    private val sessionVM: ForegroundSessionViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null && savedInstanceState.getString(AppNames.EXTRA_ACCOUNT_ID) != null) {
            accountID = savedInstanceState.getString(AppNames.EXTRA_ACCOUNT_ID)!!
        } else {
            requireActivity().intent.extras?.let { extras ->
                extras[AppNames.EXTRA_STATE]?.let {
                    if (it is String) {
                        targetState = StateID.fromId(it)
                        accountID = targetState!!.accountId
                    }
                } ?: {
                    extras[AppNames.EXTRA_ACCOUNT_ID]?.let {
                        if (it is String) {
                            accountID = it
                            targetState = null
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_choose_workspace, container, false
        )

/*
        val viewModelFactory = ForegroundSessionViewModel.ForegroundSessionViewModelFactory(
            CellsApp.instance.accountService,
            CellsApp.instance.nodeService,
            accountID,
            requireActivity().application,
        )

        val tmpVM: ForegroundSessionViewModel by viewModels { viewModelFactory }
        sessionVM = tmpVM
*/

        val adapter = WorkspaceListAdapter { slug, action -> onWsClicked(slug, action) }
        binding.workspaces.adapter = adapter
        sessionVM.liveSession.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    val currWss = it.workspaces ?: listOf()
                    adapter.submitList(currWss.sorted())
                }
            },
        )
        return binding.root
    }

    private fun onWsClicked(slug: String, command: String) {
        Log.i(fTag, "ID: $slug, do $command")

        when (command) {
            BrowseActivity.actionNavigate -> {
                val state = StateID.fromId(accountID).withPath("/${slug}")
                val action = ChooseWorkspaceFragmentDirections
                    .actionChooseWsToBrowseFolder(state.id)
                binding.chooseWorkspaceFragment.findNavController().navigate(action)
            }
            else -> return // do nothing
        }
    }

    override fun onResume() {
        Log.i(fTag, "Resuming: $accountID")
        super.onResume()

        targetState?.let {
            if (it.path != null && it.path.length > 1) {
                // We have more than an account ID,
                // directly try to navigate to the correct location
                val action = ChooseWorkspaceFragmentDirections
                    .actionChooseWsToBrowseFolder(it.id)
                targetState = null
                binding.chooseWorkspaceFragment.findNavController().navigate(action)
                return@onResume
            }
        }

        val currState = StateID.fromId(accountID)
        CellsApp.instance.wasHere(currState)

        (requireActivity() as BrowseActivity).supportActionBar?.let {
            it.title = currState.toString()
            // it.subtitle = resources.getString(R.string.ws_list_subtitle)
        }

        sessionVM.resume()
    }

    override fun onPause() {
        Log.i(fTag, "Pausing: $accountID")
        super.onPause()
        sessionVM.pause()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable(AppNames.EXTRA_ACCOUNT_ID, accountID)
        super.onSaveInstanceState(savedInstanceState)
    }
}
