package org.sinou.android.pydia.ui.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pydio.cells.transport.StateID
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentPickSessionBinding

class PickSessionFragment : Fragment() {

    // private val fTag = PickSessionFragment::class.java.simpleName

    private lateinit var binding: FragmentPickSessionBinding
    private val chooseTargetVM: ChooseTargetViewModel by viewModel()
    private val targetSessionVM: PickSessionViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pick_session, container, false
        )
        setHasOptionsMenu(true)

//        val viewModelFactory = PickSessionViewModel.TargetAccountViewModelFactory(
//            CellsApp.instance.accountService,
//            requireActivity().application,
//        )
//        val tmpVM: PickSessionViewModel by viewModels { viewModelFactory }
//        targetAccountVM = tmpVM

//        val chooseTargetFactory = ChooseTargetViewModel.ChooseTargetViewModelFactory(
//            CellsApp.instance.transferService,
//            requireActivity().application,
//        )
//        val tmpAVM: ChooseTargetViewModel by activityViewModels { chooseTargetFactory }
//        chooseTargetVM = tmpAVM

        val adapter = SessionListAdapter { stateID, action -> onClicked(stateID, action) }
        binding.sessions.adapter = adapter
        targetSessionVM.sessions.observe(viewLifecycleOwner) { adapter.submitList(it) }

        return binding.root
    }

    private fun onClicked(stateID: StateID, command: String) {
        // Log.d(fTag, "Clicked on: $stateID, do $command")
        when (command) {
            AppNames.ACTION_OPEN -> {
                val action = PickSessionFragmentDirections.actionPickFolder(stateID.id)
                findNavController().navigate(action)
            }
            else -> return // do nothing
        }
    }

    override fun onResume() {
        super.onResume()
        chooseTargetVM.setCurrentState(null)
    }
}
