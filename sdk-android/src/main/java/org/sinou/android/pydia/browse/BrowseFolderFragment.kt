package org.sinou.android.pydia.browse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.pydio.cells.api.SdkNames
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import kotlinx.coroutines.launch
import org.sinou.android.pydia.*
import org.sinou.android.pydia.databinding.FragmentBrowseFolderBinding
import org.sinou.android.pydia.room.browse.RTreeNode
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.utils.isFolder
import java.io.File

class BrowseFolderFragment : Fragment() {

    private val fTag = "BrowseFolderFragment"

    private val args: BrowseFolderFragmentArgs by navArgs()
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

        stateID = if (savedInstanceState?.getString(AppNames.EXTRA_STATE) != null) {
            val encodedState = savedInstanceState.getString(AppNames.EXTRA_STATE)
            StateID.fromId(encodedState)
        } else {
            StateID.fromId(args.state)
        }

        val application = requireActivity().application
        val viewModelFactory = TreeFolderViewModel.TreeFolderViewModelFactory(
            CellsApp.instance.accountService,
            CellsApp.instance.nodeService,
            stateID,
            application,
        )

        val tmpVM: TreeFolderViewModel by viewModels { viewModelFactory }
        treeFolderVM = tmpVM

        val adapter = NodeListAdapter(parentStateID = stateID) { node, action ->
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
        Log.i(fTag, "ID: $stateID, do $command")

        when (command) {
            BrowseActivity.actionNavigate -> navigateTo(node)
            BrowseActivity.actionMore -> {
                val action = BrowseFolderFragmentDirections
                    .actionOpenNodeMoreMenu(
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
        CellsApp.instance.wasHere(stateID)

        (requireActivity() as BrowseActivity).supportActionBar?.let {
            it.title = if (Str.empty(stateID.fileName)) {
                stateID.workspace
            } else if ("/recycle_bin" == stateID.file) {
                resources.getString(R.string.recycle_bin_label)
            } else {
                stateID.fileName
            }

            it.setDisplayHomeAsUpEnabled(true)
        }

    }

    override fun onPause() {
        super.onPause()
        treeFolderVM.pause()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable(AppNames.EXTRA_STATE, stateID.id)
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun navigateTo(node: RTreeNode) {
        lifecycleScope.launch {

            if (isFolder(node)) {
                val action = BrowseFolderFragmentDirections.actionBrowseSelf(node.encodedState)
                binding.browseFolderFragment.findNavController().navigate(action)
            } else {
                val file = CellsApp.instance.nodeService.getGetOrDownloadFile(node)
                file?.let {
                    val intent = externallyView(requireContext(), file, node)
                    startActivity(intent)
                }
            }
        }
    }
}

/**
 * Open current file with the viewer provided by Android OS.
 *
 * Thanks to https://stackoverflow.com/questions/56598480/couldnt-find-meta-data-for-provider-with-authority
 */
fun externallyView(context: Context, file: File, node: RTreeNode) :Intent {

    val uri = FileProvider.getUriForFile(
        context,
        BuildConfig.APPLICATION_ID + ".fileprovider", file
    )

    var mime = node.mime
    if (SdkNames.NODE_MIME_DEFAULT.equals(mime)) {
        mime = NodeService.getMimeType(node.name)
    }

    return Intent().setAction(Intent.ACTION_VIEW)
        .setDataAndType(uri, mime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
