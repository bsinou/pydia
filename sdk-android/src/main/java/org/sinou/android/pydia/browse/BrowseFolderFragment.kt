package org.sinou.android.pydia.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pydio.cells.api.SdkNames
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import kotlinx.coroutines.launch
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainActivity
import org.sinou.android.pydia.MainNavDirections
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentBrowseFolderBinding
import org.sinou.android.pydia.room.browse.RTreeNode
import org.sinou.android.pydia.utils.externallyView
import org.sinou.android.pydia.utils.isFolder

class BrowseFolderFragment : Fragment() {

    companion object {
        private const val fTag = "BrowseFolderFragment"

        const val ACTION_MORE = "more"
        const val ACTION_OPEN = "open"
    }

    private lateinit var binding: FragmentBrowseFolderBinding
    private lateinit var treeFolderVM: TreeFolderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_browse_folder, container, false
        )

        val args: BrowseFolderFragmentArgs by navArgs()

        val viewModelFactory = TreeFolderViewModel.TreeFolderViewModelFactory(
            CellsApp.instance.accountService,
            CellsApp.instance.nodeService,
            StateID.fromId(args.state),
            requireActivity().application,
        )

        val tmpVM: TreeFolderViewModel by viewModels { viewModelFactory }
        treeFolderVM = tmpVM

        val adapter = NodeListAdapter { node, action ->
            onClicked(
                node,
                action
            )
        }
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

    private fun onClicked(node: RTreeNode, command: String) {
        Log.i(fTag, "ID: ${treeFolderVM.stateID}, do $command")

        when (command) {
            ACTION_OPEN -> navigateTo(node)
            ACTION_MORE -> {
                val action = BrowseFolderFragmentDirections
                    .openMoreMenu(
                        node.encodedState, when (node.mime) {
                            SdkNames.NODE_MIME_RECYCLE -> TreeNodeActionsFragment.CONTEXT_RECYCLE
                            else -> TreeNodeActionsFragment.CONTEXT_BROWSE
                        }
                    )
                binding.browseFolderFragment.findNavController().navigate(action)
            }

            else -> return // do nothing
        }
    }

    override fun onResume() {
        super.onResume()
        treeFolderVM.resume()
        CellsApp.instance.wasHere(treeFolderVM.stateID)

        (requireActivity() as MainActivity).supportActionBar?.let {
            it.title = if (Str.empty(treeFolderVM.stateID.fileName)) {
                treeFolderVM.stateID.workspace
            } else if ("/recycle_bin" == treeFolderVM.stateID.file) {
                resources.getString(R.string.recycle_bin_label)
            } else {
                treeFolderVM.stateID.fileName
            }
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onPause() {
        super.onPause()
        treeFolderVM.pause()
    }

    private fun navigateTo(node: RTreeNode) {
        lifecycleScope.launch {

            if (isFolder(node)) {
                findNavController().navigate(MainNavDirections.openFolder(node.encodedState))
            } else {
                val file = CellsApp.instance.nodeService.getOrDownloadFileToCache(node)
                file?.let {
                    val intent = externallyView(requireContext(), file, node)
                    startActivity(intent)
                }
            }
        }
    }
}
