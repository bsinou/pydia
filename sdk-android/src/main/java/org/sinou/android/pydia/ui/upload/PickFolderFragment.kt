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
import com.pydio.cells.utils.Log
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

        val adapter = FolderListAdapter(stateID) { state, action ->
            onClicked(state, action)
        }

        binding.folders.adapter = adapter

        binding.openParentFolder.setText("Open parent ...")
        binding.openParentFolder.setOnClickListener {
            Log.e(fTag, "Open parent...")
        }

        pickFolderVM.children.observe(viewLifecycleOwner) { adapter.submitList(it) }
        return binding.root
    }

    private fun onClicked(stateID: StateID, command: String) {
        when (command) {
            AppNames.ACTION_OPEN -> {
                val action = UploadNavigationDirections.actionPickFolder(stateID.id)
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
            bar.title = pickFolderVM.stateID.fileName
        }
    }


    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable(AppNames.EXTRA_STATE, pickFolderVM.stateID.id)
        super.onSaveInstanceState(savedInstanceState)
    }

}
