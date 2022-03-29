package org.sinou.android.pydia.ui.browse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.*
import org.sinou.android.pydia.databinding.FragmentBrowseFolderBinding
import org.sinou.android.pydia.db.nodes.RTreeNode
import org.sinou.android.pydia.ui.menus.TreeNodeMenuFragment
import org.sinou.android.pydia.ui.utils.LoadingDialogFragment
import org.sinou.android.pydia.utils.*

/**
 * Main fragment when browsing a given account. It displays all content of a workspaces or
 * one of its child folder, providing following features:
 * - action on a given node
 * - multi selection
 * - navigation to another folder, an external viewer for a node or a carousel to display supported
 *   files in the current folder.
 */
class BrowseFolderFragment : Fragment() {

    private val fTag = BrowseFolderFragment::class.java.simpleName
    private val args: BrowseFolderFragmentArgs by navArgs()
    private lateinit var binding: FragmentBrowseFolderBinding
    private lateinit var browseFolderVM: BrowseFolderViewModel

    // Temp solution to provide a scrim during long running operations
    private var loadingDialog: LoadingDialogFragment? = null

    // FIXME: the flag is set at the end of create view method
    private var isCreated = false

    private var mode: ActionMode? = null
    private var actionModeCallback: PrimaryActionModeCallback? = null
    private var tracker: SelectionTracker<String>? = null

    private lateinit var adapter: ListAdapter<RTreeNode, out RecyclerView.ViewHolder?>
    // private lateinit var observer: ChildObserver
    private val observer = ChildObserver()


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

        setHasOptionsMenu(true)

        tmpVM.currentFolder.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isRecycle() || it.isInRecycle()) {
                    binding.addNodeFab.visibility = View.GONE
                } else {
                    binding.addNodeFab.visibility = View.VISIBLE
//                    binding.addNodeFab.setOnClickListener { onFabClicked() }
                }
            }
        }
        // TODO workspace root is not a RTreeNode => we must handle it explicitly.
        if (tmpVM.stateID.isWorkspaceRoot) {
            binding.addNodeFab.visibility = View.VISIBLE
        }
        // Put this also in observer when the above has been fixed
        binding.addNodeFab.setOnClickListener { onFabClicked() }

        binding.nodesForceRefresh.setOnRefreshListener { tmpVM.forceRefresh() }

        tmpVM.isLoading.observe(viewLifecycleOwner) {
            binding.nodesForceRefresh.isRefreshing = it
        }
        tmpVM.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let { showLongMessage(requireContext(), msg) }
        }

        isCreated = true
        return binding.root
    }

    private fun configureRecyclerAdapter() {

        // Manage grid or linear layouts
        val prefLayout = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_LAYOUT)
        val asGrid = AppNames.RECYCLER_LAYOUT_GRID == prefLayout
        val trackerBuilder: SelectionTracker.Builder<String>?
        if (asGrid) {
            val columns = resources.getInteger(R.integer.grid_default_column_number)
            binding.nodes.layoutManager = GridLayoutManager(requireActivity(), columns)
            adapter = NodeGridAdapter { node, action -> onClicked(node, action) }
            binding.nodes.adapter = adapter
            trackerBuilder = SelectionTracker.Builder(
                "grid_multi_selection",
                binding.nodes,
                NodeGridItemKeyProvider(adapter as NodeGridAdapter),
                NodeGridItemDetailsLookup(binding.nodes),
                StorageStrategy.createStringStorage()
            )
        } else {
            binding.nodes.layoutManager = LinearLayoutManager(requireActivity())
            adapter = NodeListAdapter { node, action -> onClicked(node, action) }
            binding.nodes.adapter = adapter
            trackerBuilder = SelectionTracker.Builder(
                "list_multi_selection",
                binding.nodes,
                NodeListItemKeyProvider(adapter as NodeListAdapter),
                NodeListItemDetailsLookup(binding.nodes),
                StorageStrategy.createStringStorage()
            )
        }

        // Manage multi selection
        trackerBuilder.let {
            val tmpTracker = it.withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
            ).build()
            if (adapter is NodeListAdapter) {
                (adapter as NodeListAdapter).tracker = tmpTracker
            } else if (adapter is NodeGridAdapter) {
                (adapter as NodeGridAdapter).tracker = tmpTracker
            }
            tmpTracker.addObserver(
                object : SelectionTracker.SelectionObserver<String>() {

                    override fun onItemStateChanged(key: String, selected: Boolean) {
                        super.onItemStateChanged(key, selected)
                        Log.d(fTag, "onItemStateChanged for $key, selected: $selected")
                    }

                    override fun onSelectionChanged() {
                        super.onSelectionChanged()
                        val itemNb = tracker?.selection?.size() ?: 0
                        if (mode == null && itemNb > 0 && tracker != null) {
                            // browseFolderVM.setSelection(tracker!!.selection)
                            actionModeCallback = PrimaryActionModeCallback()
                            actionModeCallback!!.startActionMode(
                                requireView(),
                                R.menu.main_multi_select_menu,
                                object : OnActionItemClickListener {
                                    override fun onActionItemClick(item: MenuItem): Boolean {
                                        Log.e(fTag, "onActionItemClick for: ${item.title}")

                                        val selected = tracker?.selection?.map { it } ?: return true

                                        val action = BrowseFolderFragmentDirections
                                            .openMoreMenu(
                                                selected.toTypedArray(),
                                                TreeNodeMenuFragment.CONTEXT_BROWSE
                                            )
                                        findNavController().navigate(action)
                                        return true
                                    }
                                })
                        } else if (itemNb > 0) {
                            mode?.title = String.format(
                                resources.getQuantityString(
                                    R.plurals.selected_count,
                                    itemNb
                                ), itemNb
                            )
                        } else {
                            actionModeCallback?.finishActionMode()
                            actionModeCallback = null
                            mode = null
                        }
                    }
                })
            tracker = tmpTracker
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isCreated) {
            Log.e(fTag, "onSaveInstanceState for: ${browseFolderVM.stateID}")
            outState.putString(AppNames.EXTRA_STATE, browseFolderVM.stateID.id)
        }
    }

    override fun onResume() {
        Log.i(fTag, "onResume")
        super.onResume()
        dumpBackStack(fTag, parentFragmentManager)

        // We must insure the Observed LiveData has been correctly updated
        // Otherwise we won't see sort order changes directly0
        browseFolderVM.resume()
        observer?.let{
            browseFolderVM.children.removeObserver(it)
        }
        browseFolderVM.children.observe(viewLifecycleOwner, observer)

        (requireActivity() as AppCompatActivity).supportActionBar?.let { bar ->
            bar.setDisplayHomeAsUpEnabled(true)
            browseFolderVM.currentFolder.observe(viewLifecycleOwner) {

                it?.let {
                    bar.title = when {
                        it.isRecycle() -> resources.getString(R.string.recycle_bin_label)
                        else -> it.name
                    }
                }
            }
        }
    }

    override fun onPause() {
        Log.i(fTag, "onPause")
        super.onPause()
        browseFolderVM.pause()
    }

    override fun onDetach() {
        Log.i(fTag, "onDetach")
        super.onDetach()
        if (isCreated) {
            resetToHomeStateIfNecessary(parentFragmentManager, browseFolderVM.stateID)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.i(fTag, "onViewStateRestored")
        super.onViewStateRestored(savedInstanceState)
    }

    private fun onClicked(node: RTreeNode, command: String) {
        Log.i(fTag, "Clicked on ${browseFolderVM.stateID} -> $command")
        when (command) {
            AppNames.ACTION_OPEN -> navigateTo(node)
            AppNames.ACTION_MORE -> {
                val action = BrowseFolderFragmentDirections
                    .openMoreMenu(
                        arrayOf(node.encodedState),
                        if (node.isInRecycle() || node.isRecycle()) {
                            TreeNodeMenuFragment.CONTEXT_RECYCLE
                        } else {
                            TreeNodeMenuFragment.CONTEXT_BROWSE
                        }
                    )
                findNavController().navigate(action)
            }
            else -> return // Unknown action, log warning and returns
        }
    }

    private fun onFabClicked() {
        val action = BrowseFolderFragmentDirections.openMoreMenu(
            arrayOf(browseFolderVM.stateID.id),
            TreeNodeMenuFragment.CONTEXT_ADD
        )
        findNavController().navigate(action)
    }

    private fun navigateTo(node: RTreeNode) = lifecycleScope.launch {
        if (node.isFolder()) {
            CellsApp.instance.setCurrentState(StateID.fromId(node.encodedState))
            findNavController().navigate(MainNavDirections.openFolder(node.encodedState))
            return@launch
        }

        Log.i(fTag, "About to navigate to ${node.getStateID()}, mime type: ${node.mime}")

        if (node.mime.startsWith("image") || node.mime.startsWith("\"image")) {
            val intent = Intent(requireActivity(), CarouselActivity::class.java)
            intent.putExtra(AppNames.EXTRA_STATE, node.encodedState)
            startActivity(intent)

            Log.i(
                fTag,
                "It's an image, open carousel ${node.getStateID()}, mime type: ${node.mime}"
            )

            // FIXME
            return@launch
        }

        Log.i(
            fTag, "**NOT** an image, opening in external viewer:" +
                    " ${node.getStateID()}, mime type: ${node.mime}"
        )

        // TODO double check. It smells.
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        browseFolderVM.setLoading(true)
        showProgressDialog()

        val file = CellsApp.instance.nodeService.getOrDownloadFileToCache(node)

        browseFolderVM.setLoading(false)
        requireActivity().window.setFlags(
            0,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

        file?.let {
            val intent = externallyView(requireContext(), file, node)
            try {
                startActivity(intent)
                loadingDialog?.dismiss()

            } catch (e: Exception) {
                val msg = "Cannot open ${it.name} (${intent.type}) with external viewer"
                Toast.makeText(requireActivity().application, msg, Toast.LENGTH_LONG).show()
                Log.e(tag, "Call to intent failed: $msg")
                e.printStackTrace()
            }
        }
    }

    private fun showProgressDialog() {
        val fm: FragmentManager = requireActivity().supportFragmentManager
        loadingDialog = LoadingDialogFragment.newInstance()
        loadingDialog?.show(fm, "fragment_edit_name")
    }

    override fun onAttach(context: Context) {
        Log.i(fTag, "onAttach")
        super.onAttach(context)
    }

    inner class PrimaryActionModeCallback : ActionMode.Callback {

        var onActionItemClickListener: OnActionItemClickListener? = null

        @MenuRes
        private var menuResId: Int = 0

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            Log.i(tag, "onCreateActionMode")
            this@BrowseFolderFragment.mode = mode
            mode.menuInflater.inflate(menuResId, menu)
            // TODO optimistic: we rely on the fact that the action mode is always opened with a single selected element.
            mode.title = String.format(resources.getQuantityString(R.plurals.selected_count, 1), 1)
            binding.addNodeFab.visibility = View.GONE
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            Log.i(tag, "onPrepareActionMode")
            // TODO we must probably redraw the list at this point to show
            //   a more explicit "selection" layout.
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            // Clear selection
            tracker?.clearSelection()

            // Re-show the FAB if necessary
            val inRecycle = browseFolderVM.currentFolder.value?.let {
                it.isRecycle() || it.isInRecycle()
            } ?: false
            if (!inRecycle) {
                binding.addNodeFab.visibility = View.VISIBLE
            }

            this@BrowseFolderFragment.mode?.finish()
            this@BrowseFolderFragment.mode = null
            actionModeCallback = null

            Log.i(tag, "onDestroyActionMode")
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            onActionItemClickListener?.onActionItemClick(item)
            Log.i(tag, "onActionItemClicked")
            mode.finish()
            return true
        }

        fun startActionMode(
            view: View,
            @MenuRes menuResId: Int,
            listener: OnActionItemClickListener
        ) {
            this.menuResId = menuResId
            view.startActionMode(this)
            this.onActionItemClickListener = listener
        }

        fun finishActionMode() {
            Log.i(tag, "finishActionMode")
            mode?.finish()
        }
    }

    inner class ChildObserver : Observer<List<RTreeNode>> {
        override fun onChanged(it: List<RTreeNode>?) {
            it?.let {
                if (it.isEmpty()) {
                    binding.emptyContent.visibility = View.VISIBLE
                    adapter.submitList(listOf())
                } else {
                    binding.emptyContent.visibility = View.GONE
                    adapter.submitList(it)
                }
            }
        }
    }
}

interface OnActionItemClickListener {
    fun onActionItemClick(item: MenuItem): Boolean
}
