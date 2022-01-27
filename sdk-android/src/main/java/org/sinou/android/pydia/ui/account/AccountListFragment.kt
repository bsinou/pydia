package org.sinou.android.pydia.ui.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.*
import org.sinou.android.pydia.databinding.FragmentAccountListBinding
import org.sinou.android.pydia.db.account.AccountDB
import org.sinou.android.pydia.services.AuthService

class AccountListFragment : Fragment() {

    companion object {
        private const val fTag = "AccountListFragment"

        const val ACTION_LOGIN = "login"
        const val ACTION_LOGOUT = "logout"
        const val ACTION_FORGET = "forget"
        const val ACTION_OPEN = "open"
    }

    private lateinit var binding: FragmentAccountListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Get a reference to the binding object and inflate the fragment views.
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_account_list, container, false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = AccountDB.getDatabase(application)
        val viewModelFactory =
            AccountListViewModel.AccountListViewModelFactory(dataSource, application)

        val accountListViewModel: AccountListViewModel by viewModels { viewModelFactory }

        val adapter =
            AccountListAdapter { accountID, action -> onAccountClicked(accountID, action) }
        binding.accountList.adapter = adapter

        accountListViewModel.sessions.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        // TODO implement color change on session switch
        // foregroundSessionViewModel.activeSession.observe()

        binding.newAccountFab.setOnClickListener {
            // it.findNavController().navigate(R.id.server_url_destination)
            val toAuthIntent = Intent(requireActivity(), AuthActivity::class.java)
            startActivity(toAuthIntent)
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun onAccountClicked(accountID: String, action: String) {
        Log.i(fTag, "ID: $accountID, do $action")

        when (action) {
            ACTION_LOGIN -> {
                CellsApp.instance.setCurrentState(
                    StateID.fromId(accountID).withPath(AppNames.CUSTOM_PATH_ACCOUNTS)
                )
                val server = CellsApp.instance.accountService.sessionFactory.getServer(accountID)
                if (server.isLegacy) {
                    val toAuthIntent = Intent(requireActivity(), AuthActivity::class.java)
                    toAuthIntent.putExtra(AppNames.EXTRA_SERVER_URL, server.serverURL.toJson())
                    toAuthIntent.putExtra(AppNames.EXTRA_SERVER_IS_LEGACY, server.isLegacy)
                    toAuthIntent.putExtra(AppNames.EXTRA_AFTER_AUTH_ACTION, AuthService.NEXT_ACTION_ACCOUNTS)
                    startActivity(toAuthIntent)
                } else {
                    lifecycleScope.launch {
                        val toAuthIntent =
                            CellsApp.instance.accountService.authService.createOAuthIntent(
                                server.serverURL,
                                AuthService.NEXT_ACTION_ACCOUNTS
                            )
                        startActivity(toAuthIntent)
                    }
                }
            }
            ACTION_LOGOUT -> lifecycleScope.launch {
                CellsApp.instance.accountService.logoutAccount(accountID)
            }
            ACTION_FORGET -> {
                findNavController().navigate(
                    AccountListFragmentDirections
                        .actionAccountsToConfirmDeletion(accountID)
                )
            }
            ACTION_OPEN -> lifecycleScope.launch {
                CellsApp.instance.accountService.openSession(accountID)
                CellsApp.instance.setCurrentState(StateID.fromId(accountID))
                findNavController().navigate(MainNavDirections.openWorkspaces())
            }

            else -> return// do nothing
        }
    }
}
