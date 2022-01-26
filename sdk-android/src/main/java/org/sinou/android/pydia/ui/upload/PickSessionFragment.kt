package org.sinou.android.pydia.ui.upload

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentPickSessionBinding

class PickSessionFragment : Fragment() {

    private val fTag = "PickSessionFragment"

    private lateinit var binding: FragmentPickSessionBinding
    private lateinit var targetAccountVM: PickSessionViewModel
    private lateinit var chooseTargetVM: ChooseTargetViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pick_session, container, false
        )

        val viewModelFactory = PickSessionViewModel.TargetAccountViewModelFactory(
            CellsApp.instance.accountService,
            requireActivity().application,
        )

        val tmpVM: PickSessionViewModel by viewModels { viewModelFactory }
        targetAccountVM = tmpVM

        val chooseTargetFactory = ChooseTargetViewModel.ChooseTargetViewModelFactory(
            CellsApp.instance.nodeService,
            requireActivity().application,
        )

        val tmpAVM: ChooseTargetViewModel by activityViewModels { chooseTargetFactory }
        chooseTargetVM = tmpAVM

        val adapter = SessionListAdapter { stateID, action -> onClicked(stateID, action) }
        binding.sessions.adapter = adapter
        targetAccountVM.sessions.observe(viewLifecycleOwner, { adapter.submitList(it) })

        return binding.root
    }

    private fun onClicked(stateID: StateID, command: String) {
        Log.i(fTag, "ID: $stateID, do $command")

        when (command) {
            AppNames.ACTION_OPEN -> {
                val action = PickSessionFragmentDirections.actionPickWs(stateID.id)
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
