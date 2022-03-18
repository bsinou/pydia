package org.sinou.android.pydia.ui.upload

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentTransferListBinding
import org.sinou.android.pydia.db.runtime.RTransfer
import org.sinou.android.pydia.ui.browse.OfflineRootsFragmentDirections
import org.sinou.android.pydia.ui.menus.TreeNodeMenuFragment
import org.sinou.android.pydia.utils.dumpBackStack
import org.sinou.android.pydia.utils.showMessage

class TransferListFragment : Fragment() {

    private val fTag = TransferListFragment::class.java.simpleName

    private lateinit var binding: FragmentTransferListBinding
    private lateinit var transferVM: TransferViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val viewModelFactory = TransferViewModel.TransferViewModelFactory(
            CellsApp.instance.transferService,
            requireActivity().application,
        )
        val tmpVM: TransferViewModel by viewModels { viewModelFactory }
        transferVM = tmpVM

        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_transfer_list, container, false
        )

        binding.transferList.layoutManager = LinearLayoutManager(activity)
        val adapter = TransferListAdapter(this::onClicked)
        binding.transferList.adapter = adapter
        transferVM.transfers.observe(viewLifecycleOwner) { adapter.submitList(it) }

        return binding.root
    }

    private fun onClicked(node: RTransfer, command: String) {
        Log.i(fTag, "Clicked on ${node.encodedState} -> $command")
        when (command) {
            // AppNames.ACTION_OPEN -> navigateTo(node)
            AppNames.ACTION_MORE -> {
                val action = TransferListFragmentDirections.openTransferMenu(node.transferId)
                findNavController().navigate(action)
            }
            else -> return // do nothing
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val connexionAlarmBtn = menu.findItem(R.id.clear_transfer_table)
        connexionAlarmBtn.isVisible = true

        connexionAlarmBtn.setOnMenuItemClickListener {
            lifecycleScope.launch {
                transferVM.transferService.clearTerminated()
            }
            return@setOnMenuItemClickListener true
        }

    }
}
