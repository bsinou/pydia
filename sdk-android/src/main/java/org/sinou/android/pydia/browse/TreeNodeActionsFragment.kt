package org.sinou.android.pydia.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.MoreMenuBookmarksBinding
import org.sinou.android.pydia.databinding.MoreMenuBrowseBinding
import org.sinou.android.pydia.room.browse.RTreeNode
import org.sinou.android.pydia.utils.externallyView
import org.sinou.android.pydia.utils.openWith

/**
 * More menu fragment: it is used to present the end-user with various possible actions
 * depending on the context.
 */
class TreeNodeActionsFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "TreeNodeActionsFragment"

        const val CONTEXT_BROWSE = "browse"
        const val CONTEXT_RECYCLE = "from_recycle"
        const val CONTEXT_BOOKMARKS = "from_bookmarks"
        const val CONTEXT_OFFLINE = "from_offline"

        const val ACTION_OPEN_WITH = "open_with"
        const val ACTION_DOWNLOAD_TO_DEVICE = "download_to_device"
        const val ACTION_OPEN_IN_WORKSPACES = "open_in_workspaces"
        const val ACTION_RENAME = "rename"
        const val ACTION_COPY = "copy"
        const val ACTION_MOVE = "move"
        const val ACTION_DELETE = "delete"
        const val ACTION_TOGGLE_BOOKMARK = "toggle_bookmark"
        const val ACTION_TOGGLE_SHARED = "toggle_shared"
    }

    private lateinit var stateID: StateID
    private lateinit var contextType: String
    private lateinit var nodeMenuVM: NodeMenuViewModel

    private lateinit var navController: NavController

    // Only *one* of the below bindings is not null, depending on the context
    private var browseBinding: MoreMenuBrowseBinding? = null
    private var bookmarkBinding: MoreMenuBookmarksBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val args: TreeNodeActionsFragmentArgs by navArgs()
        stateID = StateID.fromId(args.state)
        contextType = args.contextType

        val application = requireActivity().application
        val factory = NodeMenuViewModel.NodeMenuViewModelFactory(
            stateID,
            contextType,
            CellsApp.instance.nodeService,
            application,
        )
        val tmpVM: NodeMenuViewModel by viewModels { factory }
        nodeMenuVM = tmpVM

        var view = when (contextType) {
            CONTEXT_BROWSE -> inflateBrowseLayout(inflater, container)
            CONTEXT_BOOKMARKS -> inflateBookmarkLayout(inflater, container)
            else -> null
        }
        return view
    }

    /* BROWSE CONTEXT */

    private fun inflateBrowseLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View {
        browseBinding = DataBindingUtil.inflate<MoreMenuBrowseBinding>(
            inflater, R.layout.more_menu_browse, container, false
        )
        val binding = browseBinding as MoreMenuBrowseBinding
        nodeMenuVM.node.observe(this, {
            it?.let {
                bind(binding, it)
            }
        })
        binding.executePendingBindings()
        return binding.root
    }

    private fun bind(binding: MoreMenuBrowseBinding, node: RTreeNode) {
        binding.node = node

        binding.openWith.setOnClickListener { onClicked(node, ACTION_OPEN_WITH) }
        binding.download.setOnClickListener { onClicked(node, ACTION_DOWNLOAD_TO_DEVICE) }
        binding.rename.setOnClickListener { onClicked(node, ACTION_RENAME) }
        binding.copyTo.setOnClickListener { onClicked(node, ACTION_COPY) }
        binding.moveTo.setOnClickListener { onClicked(node, ACTION_MOVE) }
        binding.delete.setOnClickListener { onClicked(node, ACTION_DELETE) }
        binding.bookmarkSwitch.setOnClickListener { onClicked(node, ACTION_TOGGLE_BOOKMARK) }
        binding.sharedSwitch.setOnClickListener { onClicked(node, ACTION_TOGGLE_SHARED) }

        binding.executePendingBindings()
    }

    /* BOOKMARK LIST CONTEXT */

    private fun inflateBookmarkLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View {
        bookmarkBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_bookmarks, container, false
        )
        val binding = bookmarkBinding as MoreMenuBookmarksBinding
        nodeMenuVM.node.observe(this, {
            it?.let {
                bind(binding, it)
            }
        })
        binding.executePendingBindings()
        return binding.root
    }

    private fun bind(binding: MoreMenuBookmarksBinding, node: RTreeNode) {
        binding.node = node

        binding.openWith.setOnClickListener { onClicked(node, ACTION_OPEN_WITH) }
        binding.bookmarkSwitch.setOnClickListener { onClicked(node, ACTION_TOGGLE_BOOKMARK) }
        binding.download.setOnClickListener { onClicked(node, ACTION_DOWNLOAD_TO_DEVICE) }
        binding.openInWorkspaces.setOnClickListener { onClicked(node, ACTION_OPEN_IN_WORKSPACES) }

        binding.executePendingBindings()
    }

    private fun onClicked(node: RTreeNode, actionOpenWith: String) {
        val moreMenu = this
        lifecycleScope.launch {
            when (actionOpenWith) {
                ACTION_OPEN_WITH ->
                    CellsApp.instance.nodeService.getOrDownloadFileToCache(node)?.let {
                        val intent = openWith(requireContext(), it, node)
                        startActivity(intent)
                        moreMenu.dismiss()
                    }
                ACTION_DOWNLOAD_TO_DEVICE -> {
                    CellsApp.instance.nodeService.saveToExternalStorage(node)
                    moreMenu.dismiss()
                }
                ACTION_OPEN_IN_WORKSPACES -> {
                    val action =
                        TreeNodeActionsFragmentDirections.actionMoreToBrowse(node.encodedState)
                    findNavController().navigate(action)
                }
                ACTION_RENAME -> {}
                ACTION_COPY -> {}
                ACTION_MOVE -> {}
                ACTION_DELETE -> {}
                ACTION_TOGGLE_BOOKMARK -> {
                    CellsApp.instance.nodeService.toggleBookmark(node)
                    moreMenu.dismiss()
                }
                ACTION_TOGGLE_SHARED -> {
                    // TODO ask confirmation
                    CellsApp.instance.nodeService.toggleShared(node)
                    moreMenu.dismiss()
                }
            }
        }
    }


}