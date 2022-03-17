package org.sinou.android.pydia.ui.menus

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.MoreMenuManageConnectionBinding
import org.sinou.android.pydia.tasks.loginAccount
import org.sinou.android.pydia.ui.ActiveSessionViewModel

/**
 * Menu that can be opened when current session connection is broken to explain status
 * and suggest options to the end user.
 */
class ConnectionMenuFragment : BottomSheetDialogFragment() {

    private val fTag = ConnectionMenuFragment::class.java.simpleName
    private val activeSessionVM: ActiveSessionViewModel by activityViewModels()

    private lateinit var connectionBinding: MoreMenuManageConnectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(fTag, "onCreate ${activeSessionVM.accountId}")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val accService = CellsApp.instance.accountService
        connectionBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_manage_connection, container, false
        )

        connectionBinding.session = activeSessionVM.liveSession.value
        connectionBinding.launchAuth.setOnClickListener {
            activeSessionVM.liveSession.value?.let {
                loginAccount(
                    requireActivity(),
                    accService.authService,
                    accService,
                    it
                )
                dismiss()
            }
        }
        connectionBinding.executePendingBindings()
        return connectionBinding.root
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
