package org.sinou.android.pydia.ui.transfer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.R
import org.sinou.android.pydia.UploadNavigationDirections
import org.sinou.android.pydia.databinding.FragmentPickFolderBinding
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.tasks.createFolder
import org.sinou.android.pydia.utils.showLongMessage

class PickFolderFragment : Fragment() {

    private val logTag = PickFolderFragment::class.java.simpleName

    private lateinit var binding: FragmentPickFolderBinding

    private val nodeService: NodeService by inject()
    private val pickFolderVM: PickFolderViewModel by viewModel()
    private val chooseTargetVM: ChooseTargetViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pick_folder, container, false
        )
        setHasOptionsMenu(true)

        val stateID = if (savedInstanceState?.getString(AppNames.EXTRA_STATE) != null) {
            val encodedState = savedInstanceState.getString(AppNames.EXTRA_STATE)
            StateID.fromId(encodedState)
        } else {
            val args: PickFolderFragmentArgs by navArgs()
            StateID.fromId(args.state)
        }

        pickFolderVM.afterCreate(stateID)

        val adapter = FolderListAdapter(stateID, chooseTargetVM.actionContext) { state, action ->
            onClicked(state, action)
        }

        if (Str.empty(stateID.workspace)) {
            binding.addNodeFab.visibility = View.GONE
        } else {
            binding.addNodeFab.visibility = View.VISIBLE
            binding.addNodeFab.setOnClickListener { onFabClicked() }
        }

        // Wire swipe to refresh
        binding.pickFolderForceRefresh.setOnRefreshListener {
            if (Str.empty(stateID.workspace)) { // Does nothing for the time being
                binding.pickFolderForceRefresh.isRefreshing = false
            } else {
                pickFolderVM.forceRefresh()
            }
        }

        pickFolderVM.isLoading.observe(viewLifecycleOwner) {
            binding.pickFolderForceRefresh.isRefreshing = it
        }

        pickFolderVM.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let { showLongMessage(requireContext(), msg) }
        }

        binding.folders.adapter = adapter

        pickFolderVM.children.observe(viewLifecycleOwner) { adapter.addHeaderAndSubmitList(it) }
        return binding.root
    }

    private fun onClicked(stateID: StateID, command: String) {
        when (command) {
            AppNames.ACTION_OPEN -> {
                val action = if (stateID.id == AppNames.CELLS_ROOT_ENCODED_STATE) {
                    UploadNavigationDirections.actionPickSession()
                } else {
                    UploadNavigationDirections.actionPickFolder(stateID.id)
                }
                findNavController().navigate(action)
            }
            else -> return // do nothing
        }
    }

    private fun onFabClicked() {
        createFolder(requireContext(), pickFolderVM.stateID, nodeService)
    }

    override fun onResume() {
        Log.d(logTag, "onResume: ${pickFolderVM.stateID}")
        super.onResume()
        chooseTargetVM.setCurrentState(pickFolderVM.stateID)
        pickFolderVM.resume()

        (requireActivity() as AppCompatActivity).supportActionBar?.let { bar ->
            bar.setDisplayHomeAsUpEnabled(false)
            if (pickFolderVM.stateID.fileName == null) {
                if (Str.notEmpty(pickFolderVM.stateID.workspace)) {
                    bar.title = pickFolderVM.stateID.workspace
                } else {
                    bar.title = pickFolderVM.stateID.username
                }
                bar.subtitle = pickFolderVM.stateID.serverHost
            } else {
                bar.title = pickFolderVM.stateID.fileName
                // Rather display the full path (without WS) ?
                bar.subtitle = pickFolderVM.stateID.file
                // TODO configure ellipsize
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pickFolderVM.pause()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable(AppNames.EXTRA_STATE, pickFolderVM.stateID.id)
        super.onSaveInstanceState(savedInstanceState)
    }
}
