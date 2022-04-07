package org.sinou.android.pydia.ui.browse

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainNavDirections
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentOffineRootListBinding
import org.sinou.android.pydia.db.nodes.RLiveOfflineRoot
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.ui.ActiveSessionViewModel
import org.sinou.android.pydia.ui.menus.TreeNodeMenuFragment

class OfflineRootsFragment : Fragment() {

    private val fTag = OfflineRootsFragment::class.java.simpleName

    private val nodeService: NodeService by inject()

    private val activeSessionViewModel: ActiveSessionViewModel by activityViewModels()
    private val offlineVM: OfflineRootsViewModel by viewModel()
    private lateinit var binding: FragmentOffineRootListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_offine_root_list, container, false
        )
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun onClicked(node: RLiveOfflineRoot, command: String) {
        when (command) {
            AppNames.ACTION_OPEN -> navigateTo(node)
            AppNames.ACTION_MORE -> {
                val action = OfflineRootsFragmentDirections.openMoreMenu(
                    arrayOf(node.encodedState),
                    TreeNodeMenuFragment.CONTEXT_OFFLINE
                )
                findNavController().navigate(action)
            }
            else -> return // do nothing
        }
    }

    override fun onResume() {
        super.onResume()
        val activeSession = activeSessionViewModel.liveSession.value
        Log.i(fTag, "onResume: ${activeSession?.accountID}")
        activeSession?.let { session ->
            val accountID = StateID.fromId(session.accountID)

//            val viewModelFactory = OfflineRootsViewModel.OfflineRootsViewModelFactory(
//                CellsApp.instance.nodeService,
//                accountID,
//                requireActivity().application,
//            )
//            val tmpVM: OfflineRootsViewModel by viewModels { viewModelFactory }
            offlineVM.afterCreate(accountID)

            configureRecyclerAdapter()

            binding.forceRefresh.setOnRefreshListener { offlineVM.forceRefresh() }
            offlineVM.isLoading.observe(viewLifecycleOwner) {
                binding.forceRefresh.isRefreshing = it
            }
        }
    }

    private fun configureRecyclerAdapter() {
        val prefLayout = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_LAYOUT)
        val asGrid = AppNames.RECYCLER_LAYOUT_GRID == prefLayout
        val adapter: ListAdapter<RLiveOfflineRoot, out RecyclerView.ViewHolder?>
        if (asGrid) {
            binding.offlineRootList.layoutManager = GridLayoutManager(activity, 3)
            adapter = OfflineRootsGridAdapter { node, action -> onClicked(node, action) }
        } else {
            binding.offlineRootList.layoutManager = LinearLayoutManager(requireActivity())
            adapter = OfflineRootsListAdapter { node, action -> onClicked(node, action) }
        }
        binding.offlineRootList.adapter = adapter

        offlineVM.offlineRoots.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.emptyContent.visibility = View.VISIBLE
            } else {
                binding.emptyContent.visibility = View.GONE
            }
            adapter.submitList(it)
        }
    }

    private fun navigateTo(node: RLiveOfflineRoot) =
        lifecycleScope.launch {
            if (node.isFolder()) {
                val action = MainNavDirections.openFolder(node.encodedState)
                findNavController().navigate(action)
            } else {

                // TODO implement file viewing
                Log.i(fTag, "OPEN: ${node.encodedState}")

//                val file = CellsApp.instance.nodeService.getOrDownloadFileToCache(node)
//                file?.let {
//                    val intent = externallyView(requireContext(), file, node)
//                    try {
//                        startActivity(intent)
//                        // FIXME DEBUG only
//                        val msg = "Opened ${it.name} (${intent.type}) with external viewer"
//                        Log.e(tag, "Intent success: $msg")
//                    } catch (e: Exception) {
//                        val msg = "Cannot open ${it.name} (${intent.type}) with external viewer"
//                        Toast.makeText(requireActivity().application, msg, Toast.LENGTH_LONG).show()
//                        Log.e(tag, "Call to intent failed: $msg")
//                        e.printStackTrace()
//                    }
//                }
            }
        }
}