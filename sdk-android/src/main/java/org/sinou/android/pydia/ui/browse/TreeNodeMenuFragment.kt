package org.sinou.android.pydia.ui.browse

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainNavDirections
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.*
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.tasks.*
import org.sinou.android.pydia.transfer.FileExporter
import org.sinou.android.pydia.transfer.FileImporter
import org.sinou.android.pydia.utils.showLongMessage

/**
 * More menu fragment: it is used to present the end-user with various possible actions
 * depending on the context.
 */
class TreeNodeMenuFragment : BottomSheetDialogFragment() {

    companion object {
        private val fTag = TreeNodeMenuFragment::class.java.simpleName

        const val CONTEXT_BROWSE = "browse"
        const val CONTEXT_ADD = "add"
        const val CONTEXT_RECYCLE = "from_recycle"
        const val CONTEXT_BOOKMARKS = "from_bookmarks"
        const val CONTEXT_SEARCH = "from_search"
        const val CONTEXT_OFFLINE = "from_offline"

        //         const val ACTION_OPEN_WITH = "open_with"
        const val ACTION_DOWNLOAD_TO_DEVICE = "download_to_device"
        const val ACTION_OPEN_IN_WORKSPACES = "open_in_workspaces"
        const val ACTION_OPEN_PARENT_IN_WORKSPACES = "open_parent_in_workspaces"
        const val ACTION_RENAME = "rename"
        const val ACTION_COPY = "copy"
        const val ACTION_MOVE = "move"
        const val ACTION_DELETE = "delete"
        const val ACTION_TOGGLE_BOOKMARK = "toggle_bookmark"
        const val ACTION_TOGGLE_SHARED = "toggle_shared"
        const val ACTION_EMPTY_RECYCLE = "empty_recycle"
        const val ACTION_RESTORE_FROM_RECYCLE = "restore_from_recycle"
        const val ACTION_DELETE_PERMANENTLY = "delete_permanently"
        const val ACTION_CREATE_FOLDER = "create_folder"
        const val ACTION_IMPORT_FILES = "import_files"
        const val ACTION_IMPORT_FROM_CAMERA = "import_from_camera"

    }

    private val args: TreeNodeMenuFragmentArgs by navArgs()

    private lateinit var stateID: StateID
    private lateinit var contextType: String
    private lateinit var treeNodeMenuVM: TreeNodeMenuViewModel

    // Only *one* of the below bindings is not null, depending on the context
    private var browseBinding: MoreMenuBrowseBinding? = null
    private var addBinding: MoreMenuAddBinding? = null
    private var searchBinding: MoreMenuSearchBinding? = null
    private var bookmarkBinding: MoreMenuBookmarksBinding? = null
    private var recycleBinding: MoreMenuRecycleBinding? = null

    // Contracts for file transfers to and from the device
    private lateinit var fileImporter: FileImporter
    private lateinit var fileExporter: FileExporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(fTag, "onCreate")

        stateID = StateID.fromId(args.state)
        contextType = args.contextType

        val application = requireActivity().application
        val factory = TreeNodeMenuViewModel.NodeMenuViewModelFactory(
            stateID,
            contextType,
            CellsApp.instance.nodeService,
            application,
        )
        val tmpVM: TreeNodeMenuViewModel by viewModels { factory }
        treeNodeMenuVM = tmpVM

        // Communication with the device to import files / take pictures, video, ...
        fileImporter = FileImporter(
            requireActivity().activityResultRegistry,
            CellsApp.instance.fileService,
            CellsApp.instance.nodeService,
            treeNodeMenuVM,
            fTag,
            this,
        )
        lifecycle.addObserver(fileImporter)

        fileExporter = FileExporter(
            requireActivity().activityResultRegistry,
            CellsApp.instance.nodeService,
            stateID,
            fTag,
            this,
        )
        lifecycle.addObserver(fileExporter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return when (contextType) {
            CONTEXT_BROWSE -> inflateBrowseLayout(inflater, container)
            CONTEXT_ADD -> inflateAddLayout(inflater, container)
            CONTEXT_RECYCLE -> inflateRecycleLayout(inflater, container)
            CONTEXT_BOOKMARKS -> inflateBookmarkLayout(inflater, container)
            CONTEXT_SEARCH -> inflateSearchLayout(inflater, container)
            CONTEXT_OFFLINE -> inflateOfflineLayout(inflater, container)
            else -> null
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(fTag, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(fTag, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(fTag, "onStop")
    }

    /* BROWSE CONTEXT */

    private fun inflateBrowseLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View {
        browseBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_browse, container, false
        )
        val binding = browseBinding as MoreMenuBrowseBinding
        treeNodeMenuVM.node.observe(this) {
            it?.let {
                bind(binding, it)
            }
        }
        binding.executePendingBindings()
        return binding.root
    }

    private fun bind(binding: MoreMenuBrowseBinding, node: RTreeNode) {
        binding.node = node

//         binding.openWith.setOnClickListener { onClicked(node, ACTION_OPEN_WITH) }
        binding.download.setOnClickListener { onClicked(node, ACTION_DOWNLOAD_TO_DEVICE) }
        binding.rename.setOnClickListener { onClicked(node, ACTION_RENAME) }
        binding.copyTo.setOnClickListener { onClicked(node, ACTION_COPY) }
        binding.moveTo.setOnClickListener { onClicked(node, ACTION_MOVE) }
        binding.delete.setOnClickListener { onClicked(node, ACTION_DELETE) }
        binding.bookmarkSwitch.setOnClickListener { onClicked(node, ACTION_TOGGLE_BOOKMARK) }
        binding.sharedSwitch.setOnClickListener { onClicked(node, ACTION_TOGGLE_SHARED) }

        binding.executePendingBindings()
    }

    /* OFFLINE CONTEXT */
    // We only slightly modify the "browse" default context more menu

    private fun inflateOfflineLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        browseBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_browse, container, false
        )
        val binding = browseBinding as MoreMenuBrowseBinding
        treeNodeMenuVM.node.observe(this) {
            it?.let {
                offlineBind(binding, it)
            }
        }
        binding.executePendingBindings()
        return binding.root
    }

    private fun offlineBind(binding: MoreMenuBrowseBinding, node: RTreeNode) {
        // STILL TODO
        binding.node = node
//        binding.openWith.setOnClickListener { onClicked(node, ACTION_OPEN_WITH) }
//        binding.download.setOnClickListener { onClicked(node, ACTION_DOWNLOAD_TO_DEVICE) }
//        binding.rename.setOnClickListener { onClicked(node, ACTION_RENAME) }
//        binding.copyTo.setOnClickListener { onClicked(node, ACTION_COPY) }
//        binding.moveTo.setOnClickListener { onClicked(node, ACTION_MOVE) }
//        binding.delete.setOnClickListener { onClicked(node, ACTION_DELETE) }
//        binding.bookmarkSwitch.setOnClickListener { onClicked(node, ACTION_TOGGLE_BOOKMARK) }
//        binding.sharedSwitch.setOnClickListener { onClicked(node, ACTION_TOGGLE_SHARED) }

        binding.executePendingBindings()
    }

    /* ADD LIST CONTEXT (triggered from FAB while browsing) */

    private fun inflateAddLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        addBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_add, container, false
        )
        val binding = addBinding as MoreMenuAddBinding
        treeNodeMenuVM.node.observe(this) {
            it?.let {
                bind(binding, it)
            }
        }
        binding.executePendingBindings()
        return binding.root
    }

    private fun bind(binding: MoreMenuAddBinding, node: RTreeNode) {
        binding.node = node

        binding.createFolder.setOnClickListener { onClicked(node, ACTION_CREATE_FOLDER) }
        binding.importFiles.setOnClickListener { onClicked(node, ACTION_IMPORT_FILES) }
        if (requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            binding.importFromCamera.setOnClickListener {
                onClicked(
                    node,
                    ACTION_IMPORT_FROM_CAMERA
                )
            }
        } else {
            binding.importFromCamera.visibility = View.GONE
        }

        binding.executePendingBindings()
    }

    /* SEARCH CONTEXT */

    private fun inflateSearchLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        searchBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_search, container, false
        )
        val binding = searchBinding as MoreMenuSearchBinding
        treeNodeMenuVM.node.observe(this) {
            it?.let {
                bind(binding, it)
            }
        }
        binding.executePendingBindings()
        return binding.root
    }

    private fun bind(binding: MoreMenuSearchBinding, node: RTreeNode) {
        binding.node = node

        binding.openInWorkspace.setOnClickListener { onClicked(node, ACTION_OPEN_IN_WORKSPACES) }
        binding.openParentInWorkspace.setOnClickListener {
            onClicked(node, ACTION_OPEN_PARENT_IN_WORKSPACES)
        }

        binding.executePendingBindings()
    }

    /* RECYCLE and WITHIN CONTEXT */

    private fun inflateRecycleLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        recycleBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_recycle, container, false
        )
        val binding = recycleBinding as MoreMenuRecycleBinding
        treeNodeMenuVM.node.observe(this) {
            it?.let {
                bind(binding, it)
            }
        }
        binding.executePendingBindings()
        return binding.root
    }

    private fun bind(binding: MoreMenuRecycleBinding, node: RTreeNode) {
        binding.node = node
        binding.emptyRecycle.setOnClickListener { onClicked(node, ACTION_EMPTY_RECYCLE) }
        binding.restoreFromRecycle.setOnClickListener {
            onClicked(node, ACTION_RESTORE_FROM_RECYCLE)
        }
        binding.deletePermanently.setOnClickListener { onClicked(node, ACTION_DELETE_PERMANENTLY) }
//         binding.openWith.setOnClickListener { onClicked(node, ACTION_OPEN_WITH) }
        binding.executePendingBindings()
    }

    /* BOOKMARK LIST CONTEXT */

    private fun inflateBookmarkLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        bookmarkBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_bookmarks, container, false
        )
        val binding = bookmarkBinding as MoreMenuBookmarksBinding
        treeNodeMenuVM.node.observe(this) { it?.let { bind(binding, it) } }
        binding.executePendingBindings()
        return binding.root
    }

    private fun bind(binding: MoreMenuBookmarksBinding, node: RTreeNode) {
        binding.node = node
        binding.bookmarkSwitch.setOnClickListener { onClicked(node, ACTION_TOGGLE_BOOKMARK) }
        binding.download.setOnClickListener { onClicked(node, ACTION_DOWNLOAD_TO_DEVICE) }
        binding.openInWorkspaces.setOnClickListener { onClicked(node, ACTION_OPEN_IN_WORKSPACES) }
        binding.openParentInWorkspace.setOnClickListener {
            onClicked(node, ACTION_OPEN_PARENT_IN_WORKSPACES)
        }
        binding.executePendingBindings()
    }

    private fun onClicked(node: RTreeNode, actionOpenWith: String) {
        Log.i("MoreMenu", "${node.name} -> $actionOpenWith")
        val moreMenu = this
        lifecycleScope.launch {
            when (actionOpenWith) {
                // Impact remote server
                //  TODO handle a loading state
                ACTION_CREATE_FOLDER -> {
                    createFolder(requireContext(), node)
                    moreMenu.dismiss()
                }
                ACTION_RENAME -> {
                    rename(requireContext(), node)
                    moreMenu.dismiss()
                }
                ACTION_COPY -> {}
                ACTION_MOVE -> {}
                ACTION_DELETE -> {
                    moveToRecycle(requireContext(), node)
                    moreMenu.dismiss()
                }
                ACTION_EMPTY_RECYCLE -> {
                    emptyRecycle(requireContext(), node)
                    moreMenu.dismiss()
                }
                ACTION_DELETE_PERMANENTLY -> {
                    deleteFromRecycle(requireContext(), node)
                    moreMenu.dismiss()
                }
                ACTION_RESTORE_FROM_RECYCLE -> {
                    CellsApp.instance.nodeService.restoreNode(node.getStateID())?.let {
                        showLongMessage(requireContext(), it)
                    }
                    moreMenu.dismiss()
                }
                ACTION_TOGGLE_BOOKMARK -> {
                    CellsApp.instance.nodeService.toggleBookmark(node)
                    moreMenu.dismiss()
                }
                ACTION_TOGGLE_SHARED -> {
                    // TODO ask confirmation
                    CellsApp.instance.nodeService.toggleShared(node)
                    moreMenu.dismiss()
                }
                // In-app navigation
                ACTION_OPEN_IN_WORKSPACES -> {
                    CellsApp.instance.setCurrentState(StateID.fromId(node.encodedState))
                    findNavController().navigate(MainNavDirections.openFolder(node.encodedState))
                }
                ACTION_OPEN_PARENT_IN_WORKSPACES -> {
                    val parentState = StateID.fromId(node.encodedState).parentFolder()
                    CellsApp.instance.setCurrentState(parentState)
                    findNavController().navigate(MainNavDirections.openFolder(parentState.id))
                }
                // Transfer to and from device
                ACTION_IMPORT_FILES -> {
                    fileImporter.selectFiles()
                    // dismissal must be done in the ResultContract receiver or we miss the return.
                    // moreMenu.dismiss()
                }
                ACTION_IMPORT_FROM_CAMERA -> {
                    fileImporter.takePicture(node.getStateID())
                }
                ACTION_DOWNLOAD_TO_DEVICE -> {
                    fileExporter.pickTargetLocation(node)
                }
//                ACTION_OPEN_WITH ->
//                    CellsApp.instance.nodeService.getOrDownloadFileToCache(node)?.let {
//                        val intent = openWith(requireContext(), it, node)
//                        // Insure we won't crash if there no activity to handle this kind of intent
//                        if (activity?.packageManager?.resolveActivity(intent, 0) != null) {
//                            startActivity(intent)
//                        } else {
//                            showLongMessage(requireContext(), "No app found to open this file")
//                        }
//                        moreMenu.dismiss()
//                    }
            }
        }
    }
}
