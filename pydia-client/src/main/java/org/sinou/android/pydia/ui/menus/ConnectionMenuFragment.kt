package org.sinou.android.pydia.ui.menus

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.MoreMenuManageConnectionBinding
import org.sinou.android.pydia.services.AuthService
import org.sinou.android.pydia.services.SessionFactory
import org.sinou.android.pydia.tasks.loginAccount
import org.sinou.android.pydia.ui.ActiveSessionViewModel

/**
 * Menu that can be opened when current session connection is broken to explain status
 * and suggest options to the end user.
 */
class ConnectionMenuFragment : BottomSheetDialogFragment() {

    private val logTag = ConnectionMenuFragment::class.java.simpleName

    private val authService: AuthService by inject()
    private val sessionFactory: SessionFactory by inject()

    private val activeSessionVM by sharedViewModel<ActiveSessionViewModel>()
    private lateinit var connectionBinding: MoreMenuManageConnectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(logTag, "onCreate ${activeSessionVM.accountId}")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        connectionBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_manage_connection, container, false
        )

        connectionBinding.session = activeSessionVM.liveSession.value
        connectionBinding.launchAuth.setOnClickListener {
            activeSessionVM.liveSession.value?.let {
                loginAccount(
                    requireActivity(),
                    authService,
                    sessionFactory,
                    it,
                    AuthService.NEXT_ACTION_TERMINATE,
                )
                dismiss()
            }
        }
        connectionBinding.executePendingBindings()
        return connectionBinding.root
    }
}
