package org.sinou.android.pydia.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.MoreMenuBrowseBinding
import org.sinou.android.pydia.room.browse.RTreeNode

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
        const val ACTION_TOGGLE_BOOKMARK = "toggle_bookmark"

    }

    private lateinit var stateID: StateID
    private lateinit var contextType: String
    private lateinit var nodeMenuVM: NodeMenuViewModel

    // Only *one* of the below bindings is not null, depending on the context
    private var browseBinding: MoreMenuBrowseBinding? = null

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
            else -> null
        }
        return view
    }

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
        binding.bookmarkSwitch.setOnClickListener { onClicked(node, ACTION_TOGGLE_BOOKMARK) }

        binding.executePendingBindings()
    }

    private fun onClicked(node: RTreeNode, actionOpenWith: String) {
        val moreMenu = this
        lifecycleScope.launch {
            when (actionOpenWith) {
                ACTION_OPEN_WITH ->
                    CellsApp.instance.nodeService.getGetOrDownloadFile(node)?.let {
                        val intent = externallyView(requireContext(), it, node)
                        startActivity(intent)
                    }
                ACTION_TOGGLE_BOOKMARK -> {
                    CellsApp.instance.nodeService.toggleBookmark(node)
                    moreMenu.dismiss()
                }
            }
        }
    }
}