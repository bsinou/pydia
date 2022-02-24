package org.sinou.android.pydia.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.UploadNavigationDirections
import org.sinou.android.pydia.databinding.FragmentPickFolderBinding

class PickFolderFragment : Fragment() {

    private val fTag = PickFolderFragment::class.java.simpleName

    private lateinit var binding: FragmentPickFolderBinding
    private lateinit var pickFolderVM: PickFolderViewModel
    private lateinit var chooseTargetVM: ChooseTargetViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pick_folder, container, false
        )

        val stateID = if (savedInstanceState?.getString(AppNames.EXTRA_STATE) != null) {
            val encodedState = savedInstanceState.getString(AppNames.EXTRA_STATE)
            StateID.fromId(encodedState)
        } else {
            val args: PickFolderFragmentArgs by navArgs()
            StateID.fromId(args.state)
        }

        val viewModelFactory = PickFolderViewModel.PickFolderViewModelFactory(
            stateID,
            CellsApp.instance.nodeService,
            requireActivity().application,
        )
        val tmpVM: PickFolderViewModel by viewModels { viewModelFactory }
        pickFolderVM = tmpVM

        val chooseTargetFactory = ChooseTargetViewModel.ChooseTargetViewModelFactory(
            CellsApp.instance.nodeService,
            requireActivity().application,
        )
        val tmpAVM: ChooseTargetViewModel by activityViewModels { chooseTargetFactory }
        chooseTargetVM = tmpAVM

        val adapter = FolderListAdapter(stateID, tmpAVM.actionContext) { state, action ->
            onClicked(state, action)
        }

        binding.folders.adapter = adapter

//        binding.openParentFolder.setText("Open parent...")
//        binding.openParentFolder.visibility =
//            if (stateID.isWorkspaceRoot()) View.GONE else View.VISIBLE
//        binding.openParentFolder.setOnClickListener {
//            val action = UploadNavigationDirections.actionPickFolder(stateID.parentFolder().id)
//            findNavController().navigate(action)
//            Log.e(fTag, "Open parent...")
//        }

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

    override fun onResume() {
        super.onResume()
        chooseTargetVM.setCurrentState(pickFolderVM.stateID)

        (requireActivity() as AppCompatActivity).supportActionBar?.let { bar ->
            bar.setDisplayHomeAsUpEnabled(false)
            if (pickFolderVM.stateID.fileName == null) {
                if (Str.notEmpty(pickFolderVM.stateID.workspace)) {
                    bar.title = pickFolderVM.stateID.workspace
//                    bar.subtitle =
//                        "${pickFolderVM.stateID.workspace}@${pickFolderVM.stateID.serverHost}"
                } else {
                    bar.title = "${pickFolderVM.stateID.username}"
                }
                bar.subtitle = "${pickFolderVM.stateID.serverHost}"
            } else {
                bar.title = pickFolderVM.stateID.fileName
                // Rather display the full path (without WS) ?
                bar.subtitle = pickFolderVM.stateID.file
                // TODO configure ellipsize
            }
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable(AppNames.EXTRA_STATE, pickFolderVM.stateID.id)
        super.onSaveInstanceState(savedInstanceState)
    }
}
