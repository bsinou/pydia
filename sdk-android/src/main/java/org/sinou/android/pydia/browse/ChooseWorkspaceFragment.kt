package org.sinou.android.pydia.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.BrowseActivity
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentChooseWorkspaceBinding

class ChooseWorkspaceFragment : Fragment() {

    private val TAG = "ChooseWorkspaceFragment"

    private lateinit var binding: FragmentChooseWorkspaceBinding
    private lateinit var accountID: String
    private lateinit var sessionVM: ForegroundSessionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_choose_workspace, container, false
        )

        if (savedInstanceState != null && savedInstanceState.getString(AppNames.EXTRA_ACCOUNT_ID) != null) {
            accountID = savedInstanceState.getString(AppNames.EXTRA_ACCOUNT_ID)!!
        } else {
            requireActivity().intent.extras?.let { extras ->
                extras[AppNames.EXTRA_STATE]?.let {
                    if (it is String) {
                        accountID = StateID.fromId(it).accountId
                    }
                } ?: {
                    extras[AppNames.EXTRA_ACCOUNT_ID]?.let {
                        if (it is String) {
                            accountID = it
                        }
                    }
                }
            }
        }

        val application = requireActivity().application
        val viewModelFactory = ForegroundSessionViewModel.ForegroundSessionViewModelFactory(
            CellsApp.instance.accountService,
            CellsApp.instance.nodeService,
            accountID,
            application,
        )

        val tmpVM: ForegroundSessionViewModel by viewModels { viewModelFactory }
        sessionVM = tmpVM

        val adapter = WsListAdapter { slug, action -> onWsClicked(slug, action) }
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
        Log.i(TAG, "ID: $slug, do $command")

        when (command) {
            BrowseActivity.actionNavigate -> {
                val state = StateID.fromId(accountID).withPath("/${slug}")
                val action =
                    ChooseWorkspaceFragmentDirections.actionChooseWorkspaceDestinationToBrowseListDestination(
                        state.id
                    )
                binding.chooseWorkspaceFragment.findNavController().navigate(action)
            }
            else -> return // do nothing
        }
        // Toast.makeText(requireActivity(), "pos: $accountID, action ID: $action", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        Log.i(TAG, "Resuming: $accountID")
        super.onResume()
        sessionVM.resume()
    }

    override fun onPause() {
        Log.i(TAG, "Pausing: $accountID")
        super.onPause()
        sessionVM.pause()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable(AppNames.EXTRA_ACCOUNT_ID, accountID)
        super.onSaveInstanceState(savedInstanceState)
    }
}
