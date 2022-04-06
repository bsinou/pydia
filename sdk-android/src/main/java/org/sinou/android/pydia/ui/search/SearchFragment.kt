package org.sinou.android.pydia.ui.search

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainNavDirections
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentSearchBinding
import org.sinou.android.pydia.db.nodes.RTreeNode
import org.sinou.android.pydia.ui.browse.NodeListAdapter
import org.sinou.android.pydia.ui.menus.TreeNodeMenuFragment
import org.sinou.android.pydia.utils.externallyView

class SearchFragment : Fragment() {

    private val fTag = SearchFragment::class.simpleName
    private val args: SearchFragmentArgs by navArgs()

    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchVM: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_search, container, false
        )

        val viewModelFactory = SearchViewModel.SearchViewModelFactory(
            CellsApp.instance.nodeService,
            StateID.fromId(args.state),
            requireActivity().application,
        )
        val tmpVM: SearchViewModel by viewModels { viewModelFactory }
        searchVM = tmpVM

        tmpVM.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }

        binding.swipeRefresh.setOnRefreshListener {
            // Does nothing yet.
            binding.swipeRefresh.isRefreshing = false
        }

        val adapter = NodeListAdapter { node, action -> onClicked(node, action) }
        adapter.showPath()
        binding.hits.adapter = adapter
        searchVM.hits.observe(viewLifecycleOwner) { adapter.submitList(it) }

        return binding.root
    }

//        // https://stackoverflow.com/questions/34291453/adding-searchview-in-fragment
//        // https://github.com/fossasia/open-event-attendee-android/issues/862

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        searchVM.query(args.query)
        val currActivity = requireActivity() as AppCompatActivity
        val bg = resources.getDrawable(R.drawable.bar_bg_search, requireActivity().theme)
        currActivity.supportActionBar?.let { bar ->
            bar.setBackgroundDrawable(bg)
            bar.title = "Searching: ${args.query}..."
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            currActivity.window.statusBarColor =
                resources.getColor(R.color.searchStatusBarColor, requireActivity().theme)
        }
        // Requires API level 23
        // currActivity.window.statusBarColor =  resources.getColor(R.color.danger, requireActivity().theme)
    }

    override fun onPause() {
        super.onPause()
        val currActivity = requireActivity() as AppCompatActivity
        val bg = resources.getDrawable(R.drawable.empty, requireActivity().theme)
        currActivity.supportActionBar?.setBackgroundDrawable(bg)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            currActivity.window.statusBarColor =
                resources.getColor(R.color.defaultStatusBarColor, requireActivity().theme)
        }
    }

    fun updateQuery(query: String) {
        searchVM.query(query)
    }

    private fun onClicked(node: RTreeNode, command: String) {
        Log.d(fTag, "Clicked on ${node.name} -> $command")
        when (command) {
            AppNames.ACTION_OPEN -> navigateTo(node)
            AppNames.ACTION_MORE -> {
                val action = SearchFragmentDirections.openMoreMenu(
                    arrayOf(node.encodedState), TreeNodeMenuFragment.CONTEXT_SEARCH
                )
                findNavController().navigate(action)
            }
            else -> return // Unknown action, returns
        }
    }

    private fun navigateTo(node: RTreeNode) = lifecycleScope.launch {
        if (node.isFolder()) {
            CellsApp.instance.setCurrentState(StateID.fromId(node.encodedState))
            findNavController().navigate(MainNavDirections.openFolder(node.encodedState))
            return@launch
        }
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
