package org.sinou.android.pydia.ui.menus

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainNavDirections
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.MoreMenuTransferBinding
import org.sinou.android.pydia.db.runtime.RTransfer

/**
 * Menu that presents the end user with some further actions on the current clicked transfer
 */
class TransferMenuFragment : BottomSheetDialogFragment() {

    private val fTag = TransferMenuFragment::class.java.simpleName
    private lateinit var transferMenuVM: TransferMenuViewModel
    private lateinit var binding: MoreMenuTransferBinding

    val ACTION_OPEN_PARENT_IN_WORKSPACES = "open_parent_in_workspaces"
    val ACTION_DELETE_RECORD = "delete_record"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: TransferMenuFragmentArgs by navArgs()
        val transferUid = args.transferUid
        val application = requireActivity().application
        val factory = TransferMenuViewModel.TransferMenuViewModelFactory(
            transferUid,
            CellsApp.instance.transferService,
            application,
        )
        val tmpVM: TransferMenuViewModel by viewModels { factory }
        transferMenuVM = tmpVM
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_transfer, container, false
        )
        transferMenuVM.rTransfer.observe(viewLifecycleOwner) {
            it?.let { transfer ->
                binding.rTransfer = transfer
                binding.executePendingBindings()
            }
        }

        binding.deleteRecord.setOnClickListener {
            binding.rTransfer?.let {
                onClicked(it, ACTION_DELETE_RECORD)
            }
        }
        binding.openParentInWorkspace.setOnClickListener {
            binding.rTransfer?.let {
                onClicked(it, ACTION_OPEN_PARENT_IN_WORKSPACES)
            }
        }
        return binding.root
    }

    private fun onClicked(rTransfer: RTransfer, action: String) {
        Log.i("MoreMenu", "${rTransfer.getStateId()} -> $action")
        val moreMenu = this
        lifecycleScope.launch {
            when (action) {
                // Impact remote server
                ACTION_DELETE_RECORD -> {
                    transferMenuVM.transferService.deleteRecord(rTransfer.transferId)
                    moreMenu.dismiss()
                }
                // In-app navigation
                ACTION_OPEN_PARENT_IN_WORKSPACES -> {
                    val parentState = StateID.fromId(rTransfer.encodedState).parentFolder()
                    CellsApp.instance.setCurrentState(parentState)
                    findNavController().navigate(MainNavDirections.openFolder(parentState.id))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(fTag, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(fTag, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(fTag, "onStop")
    }
}
