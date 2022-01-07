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
import androidx.navigation.fragment.navArgs
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.BrowseActivity
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentBrowseFolderBinding

class BrowseFolderFragment : Fragment() {

    private val TAG = "BrowseFolderFragment"

    val args: BrowseFolderFragmentArgs by navArgs()
    private lateinit var stateID: StateID

    private lateinit var binding: FragmentBrowseFolderBinding
    private lateinit var treeFolderVM: TreeFolderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_browse_folder, container, false
        )

        if (savedInstanceState != null && savedInstanceState.getString(AppNames.EXTRA_STATE) != null) {
            val encodedState = savedInstanceState.getString(AppNames.EXTRA_STATE)
            stateID = StateID.fromId(encodedState)
        } else {
            stateID = StateID.fromId(args.state)
        }

        Log.i(TAG, "in onCreateView for ${stateID}")

        val application = requireActivity().application
        val viewModelFactory = TreeFolderViewModel.TreeFolderViewModelFactory(
            CellsApp.instance.accountService,
            CellsApp.instance.nodeService,
            stateID,
            application,
        )

        val tmpVM: TreeFolderViewModel by viewModels { viewModelFactory }
        treeFolderVM = tmpVM

        val adapter = NodeListAdapter(parentStateID = stateID)  { stateID,  action -> onNodeClicked(stateID, action) }
        binding.nodes.adapter = adapter
        treeFolderVM.children.observe(
            viewLifecycleOwner,
            {
                // When Adapter is a List adapter
                adapter.submitList(it)
            },
        )

        return binding.root
    }

    private fun onNodeClicked(stateID: StateID, command: String) {
        Log.i(TAG, "ID: $stateID, do $command")

        when (command) {
            BrowseActivity.NAVIGATE -> {
                val action =
                    BrowseFolderFragmentDirections.actionBrowseListDestinationSelf(stateID.id)
                binding.browseFolderFragment.findNavController().navigate(action)
            }
            else -> return // do nothing
        }
        // Toast.makeText(requireActivity(), "pos: $accountID, action ID: $action", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        treeFolderVM.resume()
    }

    override fun onPause() {
        super.onPause()
        treeFolderVM.pause()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable(AppNames.EXTRA_STATE, stateID.id)
        super.onSaveInstanceState(savedInstanceState)
    }
}
