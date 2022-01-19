package org.sinou.android.pydia.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.browse.BrowseFolderFragment
import org.sinou.android.pydia.databinding.FragmentPickFolderBinding

class PickFolderFragment : Fragment() {

    private lateinit var stateID: StateID

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

        stateID = if (savedInstanceState?.getString(AppNames.EXTRA_STATE) != null) {
            val encodedState = savedInstanceState.getString(AppNames.EXTRA_STATE)
            StateID.fromId(encodedState)
        } else {
            val args: PickFolderFragmentArgs by navArgs()
            StateID.fromId(args.state)
        }

        val viewModelFactory = PickFolderViewModel.TargetFolderViewModelFactory(
            CellsApp.instance.nodeService,
            stateID,
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

        val adapter = FolderListAdapter(stateID) { stateID, action ->
            onClicked(stateID, action)
        }
        binding.folders.adapter = adapter
        pickFolderVM.children.observe(viewLifecycleOwner, { adapter.submitList(it) })
        return binding.root
    }

    private fun onClicked(stateID: StateID, command: String) {
        when (command) {
            BrowseFolderFragment.ACTION_OPEN -> navigateTo(stateID)
            else -> return // do nothing
        }
    }

    override fun onResume() {
        super.onResume()
        chooseTargetVM.setCurrentState(stateID)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable(AppNames.EXTRA_STATE, stateID.id)
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun navigateTo(stateID: StateID) {
        val action = PickFolderFragmentDirections.actionPickChild(stateID.id)
        binding.pickFolderFragment.findNavController().navigate(action)
    }
}
