package org.sinou.android.pydia.ui.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.AuthActivity
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainActivity
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentAccountListBinding
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.AuthService
import org.sinou.android.pydia.services.SessionFactory
import org.sinou.android.pydia.tasks.loginAccount
import org.sinou.android.pydia.ui.common.deleteAccount
import org.sinou.android.pydia.ui.common.logoutAccount

/**
 * Holds a list with the defined accounts and their status to switch between accounts
 * and log in and out.
 * Account details is not yet implemented
 */
class AccountListFragment : Fragment() {

    private val logTag = AccountListFragment::class.java.simpleName

    private lateinit var binding: FragmentAccountListBinding

    private val authService: AuthService by inject()
    private val sessionFactory: SessionFactory by inject()
    private val accountService: AccountService by inject()

    private val accountListViewModel: AccountListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e(logTag, "onCreateView ${savedInstanceState?.getString(AppNames.EXTRA_STATE)}")
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_account_list, container, false
        )

//        val application = requireNotNull(this.activity).application
//        val viewModelFactory = AccountListViewModel.AccountListViewModelFactory(
//            CellsApp.instance.accountService,
//            application
//        )
//        val tmp: AccountListViewModel by viewModels { viewModelFactory }
//        accountListViewModel = tmp

        val adapter = AccountListAdapter { accountID, action ->
            onAccountClicked(accountID, action)
        }
        binding.accountList.adapter = adapter

        accountListViewModel.sessions.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.emptyContent.visibility = View.VISIBLE
                binding.accountList.visibility = View.GONE
            } else {
                binding.accountList.visibility = View.VISIBLE
                binding.emptyContent.visibility = View.GONE
                adapter.submitList(it)
            }
        }

        binding.newAccountFab.setOnClickListener {
            val toAuthIntent = Intent(requireActivity(), AuthActivity::class.java)
            startActivity(toAuthIntent)
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun onAccountClicked(accountID: String, action: String) {
        Log.i(logTag, "ID: $accountID, do $action")
        when (action) {
            AppNames.ACTION_LOGIN -> {
                val currSession = accountListViewModel.sessions.value
                    ?.filter { it.accountID == accountID }
                    ?.get(0)
                if (currSession == null) {
                    Log.i(logTag, "No live session found for: $accountID in ViewModel, aborting...")
                    return
                }
                loginAccount(
                    requireContext(),
                    authService,
                    sessionFactory,
                    currSession,
                    AuthService.NEXT_ACTION_ACCOUNTS
                )
            }
            AppNames.ACTION_LOGOUT -> lifecycleScope.launch {
                logoutAccount(requireContext(), accountID, accountService)
            }
            AppNames.ACTION_FORGET -> {
                deleteAccount(requireContext(), accountID, accountService)
            }
            AppNames.ACTION_OPEN -> lifecycleScope.launch {
                accountService.openSession(accountID)
                CellsApp.instance.setCurrentState(StateID.fromId(accountID))
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.putExtra(AppNames.EXTRA_STATE, accountID)
                startActivity(intent)
            }
            else -> return // do nothing
        }
    }
}
