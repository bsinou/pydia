package org.sinou.android.pydia.ui.browse

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import kotlinx.coroutines.launch
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainNavDirections
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentBrowseFolderBinding
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.services.SessionFactory
import org.sinou.android.pydia.ui.utils.LoadingDialogFragment
import org.sinou.android.pydia.utils.*

class BrowseFolderFragment : Fragment() {

    private val fTag = BrowseFolderFragment::class.java.simpleName

    private val args: BrowseFolderFragmentArgs by navArgs()

    private lateinit var binding: FragmentBrowseFolderBinding
    private lateinit var browseFolderVM: BrowseFolderViewModel

    // Temp solution to provide a scrim during long running operations
    private var loadingDialog: LoadingDialogFragment? = null

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
        var adapter: ListAdapter<RTreeNode, out RecyclerView.ViewHolder?>
        if (asGrid) {
            binding.nodes.layoutManager = GridLayoutManager(activity, 3)
            adapter = NodeGridAdapter { node, action -> onClicked(node, action) }
        } else {
            binding.nodes.layoutManager = LinearLayoutManager(requireActivity())
            adapter = NodeListAdapter { node, action -> onClicked(node, action) }
        }
        binding.nodes.adapter = adapter
        browseFolderVM.children.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.emptyContent.visibility = View.VISIBLE
                binding.nodes.visibility = View.GONE
            } else {
                binding.nodes.visibility = View.VISIBLE
                binding.emptyContent.visibility = View.GONE
                adapter.submitList(it)
            }
        }
    }

    private fun onClicked(node: RTreeNode, command: String) {
        Log.i(fTag, "Clicked on ${browseFolderVM.stateID} -> $command")
        when (command) {
            AppNames.ACTION_OPEN -> navigateTo(node)
            AppNames.ACTION_MORE -> {
                val action = BrowseFolderFragmentDirections
                    .openMoreMenu(
                        node.encodedState,
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
            browseFolderVM.stateID.id,
            TreeNodeMenuFragment.CONTEXT_ADD
        )
        findNavController().navigate(action)
    }

    override fun onResume() {
        Log.i(fTag, "onResume")
        super.onResume()
        dumpBackStack(fTag, parentFragmentManager)

        browseFolderVM.resume()

        (requireActivity() as AppCompatActivity).supportActionBar?.let {
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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.i(fTag, "onViewStateRestored")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onPause() {
        Log.i(fTag, "onPause")
        super.onPause()
        browseFolderVM.pause()
    }

    override fun onDetach() {
        Log.i(fTag, "onDetach")
        super.onDetach()
        resetToHomeStateIfNecessary(parentFragmentManager, browseFolderVM.stateID)
    }

    override fun onAttach(context: Context) {
        Log.i(fTag, "onAttach")
        super.onAttach(context)
    }

    private fun navigateTo(node: RTreeNode) = lifecycleScope.launch {
        if (node.isFolder()) {
            CellsApp.instance.setCurrentState(StateID.fromId(node.encodedState))
            findNavController().navigate(MainNavDirections.openFolder(node.encodedState))
            return@launch
        }

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

}
