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
import androidx.navigation.fragment.findNavController
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.BrowseActivity
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentChooseWorkspaceBinding

class ChooseWorkspaceFragment : Fragment() {

    private val fTag = "ChooseWorkspaceFragment"

    private lateinit var sessionVM: SessionViewModel
    private lateinit var binding: FragmentChooseWorkspaceBinding
    private lateinit var accountID: StateID

    private var targetState: StateID? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null && savedInstanceState.getString(AppNames.EXTRA_ACCOUNT_ID) != null) {
            accountID = StateID.fromId(savedInstanceState.getString(AppNames.EXTRA_ACCOUNT_ID))!!
        } else {
            requireActivity().intent.extras?.let { extras ->
                extras[AppNames.EXTRA_STATE]?.let {
                    if (it is String) {
                        targetState = StateID.fromId(it)
                        accountID = StateID.fromId(targetState!!.accountId)
                    }
                } ?: {
                    extras[AppNames.EXTRA_ACCOUNT_ID]?.let {
                        if (it is String) {
                            accountID = StateID.fromId(it)
                            targetState = null
                        }
                    }
                }
            }
        }

        val factory = SessionViewModel.SessionViewModelFactory(
            accountID,
            requireActivity().application,
        )
        val tmpVM: SessionViewModel by activityViewModels { factory }
        sessionVM = tmpVM
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

        sessionVM.liveSession.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    val currWss = it.workspaces ?: listOf()
                    adapter.submitList(currWss.sorted())
                }
            },
        )
    }

    private fun onWsClicked(slug: String, command: String) {
        when (command) {
            BrowseActivity.actionNavigate -> {
                val state = sessionVM.accountID.withPath("/${slug}")
                val action = ChooseWorkspaceFragmentDirections
                    .actionOpenWorkspace(state.id)
                findNavController().navigate(action)
            }
            else -> return // do nothing
        }
    }

    override fun onResume() {
        Log.i(fTag, "onResume: ${sessionVM.accountID}")

        super.onResume()

        targetState?.let {
            if (it.path != null && it.path.length > 1) {
                Log.i(fTag, "onResume, we have a path: ${it.path}. Navigating")
                val action = ChooseWorkspaceFragmentDirections
                    .actionOpenWorkspace(it.id)
                targetState = null
                binding.chooseWorkspaceFragment.findNavController().navigate(action)
                return@onResume
            }
        }

        (requireActivity() as BrowseActivity).supportActionBar?.let {
            it.title = sessionVM.accountID.toString()
            // it.subtitle = resources.getString(R.string.ws_list_subtitle)
        }

        CellsApp.instance.wasHere(sessionVM.accountID)
        sessionVM.resume()
    }

    override fun onPause() {
        Log.i(fTag, "Pausing: ${sessionVM.accountID}")
        super.onPause()
        sessionVM.pause()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable(AppNames.EXTRA_ACCOUNT_ID, accountID.id)
        super.onSaveInstanceState(savedInstanceState)
    }
}
