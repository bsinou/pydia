package org.sinou.android.pydia.ui.search

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pydio.cells.api.SdkNames
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainNavDirections
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentSearchBinding
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.ui.browse.BrowseFolderFragment
import org.sinou.android.pydia.ui.browse.BrowseFolderFragmentDirections
import org.sinou.android.pydia.ui.browse.NodeListAdapter
import org.sinou.android.pydia.ui.browse.TreeNodeMenuFragment
import org.sinou.android.pydia.utils.externallyView
import org.sinou.android.pydia.utils.isFolder

class SearchFragment : Fragment() {

    private val fTag = "SearchFragment"
    private val args: SearchFragmentArgs by navArgs()

    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchVM: SearchViewModel

    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null

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

        val adapter = NodeListAdapter { node, action -> onClicked(node, action) }
        binding.hits.adapter = adapter
        searchVM.hits.observe(viewLifecycleOwner, { adapter.submitList(it) })

        return binding.root
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        // Inflate the menu; this adds items to the action bar if it is present.
//        inflater.inflate(R.menu.main_options, menu)
//        Log.i(fTag, "### option menu created")
//
//        val searchItem = menu.findItem(R.id.search)
//        if (searchItem != null) {
//            searchView = searchItem.getActionView() as SearchView
//        }
//
//        if (searchView == null) {
//            // should never happen
//            Log.i(fTag, "Search view not found, aborting ")
//        }
//
//        val queryTextListener: SearchView.OnQueryTextListener = object :
//            SearchView.OnQueryTextListener {
//            override fun onQueryTextChange(newText: String): Boolean {
//                Log.i("onQueryTextChange", newText)
//                return true
//            }
//
//            override fun onQueryTextSubmit(query: String): Boolean {
//                Log.i("onQueryTextChange", query)
//                return true
//            }
//        }
//
//        // searchView!!.setQuery(args.query, false)
//        searchView!!.setOnQueryTextListener(queryTextListener)
//
//        // https://stackoverflow.com/questions/34291453/adding-searchview-in-fragment
//        // https://github.com/fossasia/open-event-attendee-android/issues/862
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        searchVM.query(args.query)
    }

//    override fun onNewIntent(intent: Intent) {
//        setIntent(intent)
//        handleIntent(intent)
//    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                doSearch(query)
            }
        }
    }

    private fun doSearch(query: String) {
        Log.e(fTag, "Do search: $query")
        searchVM.query(query)
    }

    private fun onClicked(node: RTreeNode, command: String) {
        Log.i(fTag, "Clicked on ${node.name} -> $command")
        when (command) {
            BrowseFolderFragment.ACTION_OPEN -> navigateTo(node)
           /* BrowseFolderFragment.ACTION_MORE -> {
                val action = BrowseFolderFragmentDirections
                    .openMoreMenu(
                        node.encodedState, when (node.mime) {
                            SdkNames.NODE_MIME_RECYCLE -> TreeNodeMenuFragment.CONTEXT_RECYCLE
                            else -> TreeNodeMenuFragment.CONTEXT_BROWSE
                        }
                    )
                findNavController().navigate(action)
            }*/

            else -> return // Unknown action, log warning and returns
        }
    }

    private fun navigateTo(node: RTreeNode) = lifecycleScope.launch {
        if (isFolder(node)) {
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