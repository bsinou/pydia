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
import androidx.navigation.fragment.navArgs
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentPickWsBinding

class PickWSFragment : Fragment() {

    private val fTag = "PickWSFragment"

    private lateinit var stateID: StateID
    private lateinit var binding: FragmentPickWsBinding
    private lateinit var targetWsVM: PickWSViewModel
    private lateinit var chooseTargetVM: ChooseTargetViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pick_ws, container, false
        )

        stateID = if (savedInstanceState?.getString(AppNames.EXTRA_STATE) != null) {
            val encodedState = savedInstanceState.getString(AppNames.EXTRA_STATE)
            StateID.fromId(encodedState)
        } else {
            val args: PickWSFragmentArgs by navArgs()
            StateID.fromId(args.state)
        }

        val viewModelFactory = PickWSViewModel.TargetWorkspaceViewModelFactory(
            CellsApp.instance.accountService,
            stateID,
            requireActivity().application,
        )
        val tmpVM: PickWSViewModel by viewModels { viewModelFactory }
        targetWsVM = tmpVM

        val chooseTargetFactory = ChooseTargetViewModel.ChooseTargetViewModelFactory(
            CellsApp.instance.nodeService,
            requireActivity().application,
        )
        val tmpAVM: ChooseTargetViewModel by activityViewModels { chooseTargetFactory }
        chooseTargetVM = tmpAVM

        val adapter = WorkspaceListAdapter { slug, action -> onClicked(slug, action) }
        binding.workspaces.adapter = adapter

        targetWsVM.liveSession.observe(
            viewLifecycleOwner,
        ) {
            it?.let {
                val currWss = it.workspaces ?: listOf()
                adapter.submitList(currWss.sorted())
            }
        }
        return binding.root
    }

    private fun onClicked(slug: String, command: String) {
        when (command) {
            AppNames.ACTION_OPEN -> navigateTo(stateID.withPath("/${slug}"))
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
        val action = PickWSFragmentDirections.actionPickFolder(stateID.id)
        findNavController().navigate(action)
    }
}
