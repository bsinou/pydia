package org.sinou.android.pydia.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import kotlinx.coroutines.launch
import org.sinou.android.pydia.BrowseActivity
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentBookmarkListBinding
import org.sinou.android.pydia.room.browse.RTreeNode
import org.sinou.android.pydia.utils.externallyView
import org.sinou.android.pydia.utils.isFolder

class BookmarksFragment : Fragment() {

    private val fTag = "BookmarksFragment"

    private lateinit var binding: FragmentBookmarkListBinding
    private val sessionVM: ForegroundSessionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_bookmark_list, container, false
        )

        Log.i(fTag, "onCreateView: ${sessionVM.accountID}")

        val adapter = NodeListAdapter { node, action -> onClicked(node,action) }
        binding.bookmarkList.adapter = adapter
        sessionVM.bookmarks.observe(viewLifecycleOwner, { adapter.submitList(it)} )

        return binding.root
    }

    private fun onClicked(node: RTreeNode, command: String) {

        when (command) {
            BrowseActivity.actionNavigate -> navigateTo(node)
            BrowseActivity.actionMore -> {
                val action = BrowseFolderFragmentDirections
                    .actionOpenNodeMoreMenu( node.encodedState, TreeNodeActionsFragment.CONTEXT_BOOKMARKS)
                binding.bookmarkListFragment.findNavController().navigate(action)
            }
            else -> return // do nothing
        }
    }

    private fun navigateTo(node: RTreeNode) {
        lifecycleScope.launch {
            if (isFolder(node)) {
                val action = BookmarksFragmentDirections.actionBookmarkToBrowse(node.encodedState)
                binding.bookmarkListFragment.findNavController().navigate(action)
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