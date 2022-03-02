package org.sinou.android.pydia.ui.upload

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainNavDirections
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentTransferListBinding
import org.sinou.android.pydia.db.runtime.RUpload
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

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_transfer_list, container, false
        )

        val viewModelFactory = TransferViewModel.TransferViewModelFactory(
            CellsApp.instance.transferService,
            requireActivity().application,
        )
        val tmpVM: TransferViewModel by viewModels { viewModelFactory }
        transferVM = tmpVM

        binding.transferList.layoutManager = LinearLayoutManager(activity)
        val adapter = TransferListAdapter(this::onClicked)
        binding.transferList.adapter = adapter
        transferVM.transfers.observe(viewLifecycleOwner) { adapter.submitList(it) }

//        binding.forceLaunch.setOnClickListener {
//            lifecycleScope.launch {
//                CellsApp.instance.nodeService.uploadAllNew()
//            }
//        }

        binding.clearList.setOnClickListener {
            showMessage(requireContext(), "to do: clear out the list")
        }

        return binding.root
    }

    private fun onClicked(node: RUpload, command: String) {
        Log.i(fTag, "Clicked on ${node.encodedState} -> $command")
    }

    override fun onResume() {
        super.onResume()
        dumpBackStack(fTag, parentFragmentManager)
        // transferVM.resume()
    }

//    override fun onPause() {
//        super.onPause()
//        // transferVM.pause()
//    }
}
