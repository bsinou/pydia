package org.sinou.android.pydia.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.*
import org.sinou.android.pydia.databinding.FragmentBookmarkListBinding
import org.sinou.android.pydia.room.browse.RTreeNode
import org.sinou.android.pydia.utils.BackStackAdapter
import org.sinou.android.pydia.utils.externallyView
import org.sinou.android.pydia.utils.isFolder

class BookmarksFragment : Fragment() {

    private val fTag = "BookmarksFragment"

    private lateinit var binding: FragmentBookmarkListBinding
    private val activeSessionViewModel: ActiveSessionViewModel by activityViewModels()

    private val backPressedCallback = BackStackAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_bookmark_list, container, false
        )
        return binding.root
    }

    private fun onClicked(node: RTreeNode, command: String) {
        when (command) {
            BrowseFolderFragment.ACTION_OPEN -> navigateTo(node)
            BrowseFolderFragment.ACTION_MORE -> {
                val action = BookmarksFragmentDirections.openMoreMenu(
                    node.encodedState,
                    TreeNodeActionsFragment.CONTEXT_BOOKMARKS
                )
                findNavController().navigate(action)
            }
            else -> return // do nothing
        }
    }

    override fun onResume() {
        super.onResume()

        val activeSession = activeSessionViewModel.activeSession.value
        Log.i(fTag, "onResume: ${activeSession?.accountID}")
        activeSession?.let { session ->
            val accountID = StateID.fromId(session.accountID)
            (requireActivity() as MainActivity).supportActionBar?.let {
                it.title = "Bookmarks" // accountID.toString()
            }

            val adapter = NodeListAdapter { node, action -> onClicked(node, action) }
            binding.bookmarkList.adapter = adapter

            val viewModelFactory = BookmarksViewModel.BookmarksViewModelFactory(
                accountID, requireActivity().application,
            )
            val bookmarksViewModel: BookmarksViewModel by viewModels { viewModelFactory }

            val currentState = accountID.withPath(AppNames.CUSTOM_PATH_BOOKMARKS)
            CellsApp.instance.setCurrentState(currentState)

            backPressedCallback.initializeBackNavigation(parentFragmentManager, currentState)

            bookmarksViewModel.bookmarks.observe(viewLifecycleOwner, { adapter.submitList(it) })
        }
    }

    private fun navigateTo(node: RTreeNode) {
        lifecycleScope.launch {
            if (isFolder(node)) {
                val action = MainNavDirections.openFolder(node.encodedState)
                findNavController().navigate(action)
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