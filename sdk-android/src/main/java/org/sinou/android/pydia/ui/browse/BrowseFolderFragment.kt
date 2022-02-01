package org.sinou.android.pydia.ui.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.pydio.cells.api.SdkNames
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import kotlinx.coroutines.launch
import org.sinou.android.pydia.*
import org.sinou.android.pydia.databinding.FragmentBrowseFolderBinding
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.transfer.FileImporter
import org.sinou.android.pydia.utils.*

class BrowseFolderFragment : Fragment() {

    companion object {
        private const val fTag = "BrowseFolderFragment"
    }

    private val args: BrowseFolderFragmentArgs by navArgs()

    private lateinit var binding: FragmentBrowseFolderBinding
    private lateinit var browseFolderVM: BrowseFolderViewModel

    // Contracts for file transfers to and from the device
    // private lateinit var fileImporter: FileImporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Communication with the device to import files / take pictures, video, ...
    /*    fileImporter = FileImporter(
            requireActivity().activityResultRegistry,
            CellsApp.instance.nodeService,
            null,
            fTag,
        )

        // Hmmm it smells: we should rather attach this to the fragment,
        // but when attached to the more menu, we skip the results.
        // requireActivity().lifecycle.addObserver(fileImporter)
        lifecycle.addObserver(fileImporter)*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_browse_folder, container, false
        )

        val viewModelFactory = BrowseFolderViewModel.BrowseFolderViewModelFactory(
            CellsApp.instance.accountService,
            CellsApp.instance.nodeService,
            StateID.fromId(args.state),
            requireActivity().application,
        )
        val tmpVM: BrowseFolderViewModel by viewModels { viewModelFactory }
        browseFolderVM = tmpVM

        configureRecyclerAdapter()

        val backPressedCallback = BackStackAdapter.initialised(
            parentFragmentManager,
            findNavController(),
            StateID.fromId(args.state)
        )
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )

        browseFolderVM.currentFolder.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isRecycle() || it.isInRecycle()) {
                    binding.addNodeFab.visibility = View.GONE
                } else {
                    binding.addNodeFab.visibility = View.VISIBLE
//                    binding.addNodeFab.setOnClickListener { onFabClicked() }
                }
            }
        }
        setHasOptionsMenu(true)

        // TODO workspace root is not a RTreeNode => we must handle it explicitly.
        if (browseFolderVM.stateID.isWorkspaceRoot) {
            binding.addNodeFab.visibility = View.VISIBLE
        }
        // Put this also in observer when the above has been fixed
        binding.addNodeFab.setOnClickListener { onFabClicked() }

        return binding.root
    }

    private fun configureRecyclerAdapter() {
        val prefLayout = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_LAYOUT)
        val asGrid = AppNames.RECYCLER_LAYOUT_GRID == prefLayout
        if (asGrid) {
            binding.nodes.layoutManager = GridLayoutManager(activity, 3)
            val adapter = NodeGridAdapter { node, action -> onClicked(node, action) }
            binding.nodes.adapter = adapter
            browseFolderVM.children.observe(viewLifecycleOwner) { adapter.submitList(it) }
        } else {
            binding.nodes.layoutManager = LinearLayoutManager(activity)
            val adapter = NodeListAdapter { node, action -> onClicked(node, action) }
            binding.nodes.adapter = adapter
            browseFolderVM.children.observe(viewLifecycleOwner) { adapter.submitList(it) }
        }
    }

    private fun onClicked(node: RTreeNode, command: String) {
        Log.i(fTag, "Clicked on ${browseFolderVM.stateID} -> $command")
        when (command) {
            AppNames.ACTION_OPEN -> navigateTo(node)
            AppNames.ACTION_MORE -> {
                val action = BrowseFolderFragmentDirections
                    .openMoreMenu(
                        node.encodedState, when (node.mime) {
                            SdkNames.NODE_MIME_RECYCLE -> TreeNodeMenuFragment.CONTEXT_RECYCLE
                            else -> TreeNodeMenuFragment.CONTEXT_BROWSE
                        }
                    )
                findNavController().navigate(action)
            }

            else -> return // Unknown action, log warning and returns
        }
    }

    private fun onFabClicked() {
        val action = BrowseFolderFragmentDirections.openMoreMenu(
            browseFolderVM.stateID.id,
            TreeNodeMenuFragment.CONTEXT_ADD
        )
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        dumpBackStack(fTag, parentFragmentManager)

        browseFolderVM.resume()

        // FIXME we should not statically link MainActivity here.
        (requireActivity() as MainActivity).supportActionBar?.let {
            it.title = when {
                Str.empty(browseFolderVM.stateID.fileName) -> {
                    browseFolderVM.stateID.workspace
                }
                "/recycle_bin" == browseFolderVM.stateID.file -> {
                    resources.getString(R.string.recycle_bin_label)
                }
                else -> {
                    browseFolderVM.stateID.fileName
                }
            }
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onPause() {
        super.onPause()
        browseFolderVM.pause()
    }

    override fun onDetach() {
        Log.i(fTag, "... About to detach:")
        super.onDetach()
        resetToHomeStateIfNecessary(parentFragmentManager, browseFolderVM.stateID)
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
