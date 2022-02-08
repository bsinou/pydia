package org.sinou.android.pydia.ui.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainNavDirections
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentBookmarkListBinding
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.utils.BackStackAdapter
import org.sinou.android.pydia.utils.externallyView
import org.sinou.android.pydia.utils.resetToHomeStateIfNecessary

class BookmarksFragment : Fragment() {

    private val fTag = BookmarksFragment::class.java.simpleName

    private val activeSessionViewModel: ActiveSessionViewModel by activityViewModels()

    private lateinit var binding: FragmentBookmarkListBinding
    private var bookmarksVM: BookmarksViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_bookmark_list, container, false
        )
        findNavController().addOnDestinationChangedListener(ChangeListener())

        return binding.root
    }

    private fun onClicked(node: RTreeNode, command: String) {
        when (command) {
            AppNames.ACTION_OPEN -> navigateTo(node)
            AppNames.ACTION_MORE -> {
                val action = BookmarksFragmentDirections.openMoreMenu(
                    node.encodedState,
                    TreeNodeMenuFragment.CONTEXT_BOOKMARKS
                )
                findNavController().navigate(action)
            }
            else -> return // do nothing
        }
    }

    override fun onResume() {
        super.onResume()
//        dumpBackStack(fTag, parentFragmentManager)


//        (requireActivity() as AppCompatActivity).supportActionBar?.let {
//            it.title = "Bookmarks" // accountID.toString()
//        }


        val activeSession = activeSessionViewModel.liveSession.value
        Log.i(fTag, "onResume: ${activeSession?.accountID}")
        activeSession?.let { session ->
            val accountID = StateID.fromId(session.accountID)

            val viewModelFactory = BookmarksViewModel.BookmarksViewModelFactory(
                CellsApp.instance.nodeService,
                accountID,
                requireActivity().application,
            )
            val tmp: BookmarksViewModel by viewModels { viewModelFactory }
            bookmarksVM = tmp
            configureRecyclerAdapter(tmp)

            tmp.triggerRefresh()

            val currentState = accountID.withPath(AppNames.CUSTOM_PATH_BOOKMARKS)
            CellsApp.instance.setCurrentState(currentState)
            val backPressedCallback = BackStackAdapter.initialised(
                parentFragmentManager,
                findNavController(),
                currentState
            )
            requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
        }
    }

    private fun configureRecyclerAdapter(viewModel: BookmarksViewModel) {
        val prefLayout = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_LAYOUT)
        val asGrid = AppNames.RECYCLER_LAYOUT_GRID == prefLayout
        val adapter: ListAdapter<RTreeNode, out RecyclerView.ViewHolder?>
        if (asGrid) {
            binding.bookmarkList.layoutManager = GridLayoutManager(activity, 3)
            adapter = NodeGridAdapter { node, action -> onClicked(node, action) }
        } else {
            binding.bookmarkList.layoutManager = LinearLayoutManager(requireActivity())
            adapter = NodeListAdapter { node, action -> onClicked(node, action) }
        }
        binding.bookmarkList.adapter = adapter
        viewModel.bookmarks.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.emptyContent.visibility = View.VISIBLE
                binding.bookmarkList.visibility = View.GONE
            } else {
                binding.bookmarkList.visibility = View.VISIBLE
                binding.emptyContent.visibility = View.GONE
                adapter.submitList(it)
            }
        }
    }

    override fun onDetach() {
        bookmarksVM?.let {
            resetToHomeStateIfNecessary(
                parentFragmentManager,
                it.stateID.withPath("/${AppNames.CUSTOM_PATH_BOOKMARKS}")
            )
        }
        super.onDetach()
    }

    private fun navigateTo(node: RTreeNode) =
        lifecycleScope.launch {
            if (node.isFolder()) {
                val action = MainNavDirections.openFolder(node.encodedState)
                findNavController().navigate(action)
            } else {
                val file = CellsApp.instance.nodeService.getOrDownloadFileToCache(node)
                file?.let {
                    val intent = externallyView(requireContext(), file, node)
                    try {
                        startActivity(intent)
                        // FIXME DEBUG only
                        val msg = "Opened ${it.name} (${intent.type}) with external viewer"
                        Log.e(tag, "Intent success: $msg")
                    } catch (e: Exception) {
                        val msg = "Cannot open ${it.name} (${intent.type}) with external viewer"
                        Toast.makeText(requireActivity().application, msg, Toast.LENGTH_LONG).show()
                        Log.e(tag, "Call to intent failed: $msg")
                        e.printStackTrace()
                    }
                }
            }
        }
}

private class ChangeListener : NavController.OnDestinationChangedListener {

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        Log.i("ChangeListener", "destination changed")
    }
}