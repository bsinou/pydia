package org.sinou.android.pydia.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.MoreMenuBrowseBinding

/**
 * More menu fragment: it is used to present the end-user with various possible actions
 * depending on the context.
 */
class TreeNodeActionsFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "FileActionsFragment"

        const val CONTEXT_BROWSE = "browse"
        const val CONTEXT_RECYCLE = "from_recycle"
        const val CONTEXT_BOOKMARKS = "from_bookmarks"
        const val CONTEXT_OFFLINE = "from_offline"
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

        var view: View? = null
        when (contextType) {
            CONTEXT_BROWSE -> {
                browseBinding = DataBindingUtil.inflate<MoreMenuBrowseBinding>(
                    inflater, R.layout.more_menu_browse, container, false
                )
                val binding = browseBinding as MoreMenuBrowseBinding
                view = binding.root
                nodeMenuVM.node.observe(this, {
                    it?.let {
                        binding.node = it
                        binding.executePendingBindings()
                    }
                })
                binding.executePendingBindings()
            }
        }
        return view
    }
}