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
import org.sinou.android.pydia.services.AuthService
import org.sinou.android.pydia.ui.common.deleteAccount

class AccountListFragment : Fragment() {

    private val fTag = AccountListFragment::class.java.simpleName

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
        val viewModelFactory = AccountListViewModel.AccountListViewModelFactory(
            CellsApp.instance.accountService,
            application
        )
        val accountListViewModel: AccountListViewModel by viewModels { viewModelFactory }

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
        Log.i(fTag, "ID: $accountID, do $action")
        when (action) {
            AppNames.ACTION_LOGIN -> {
                CellsApp.instance.setCurrentState(
                    StateID.fromId(accountID).withPath(AppNames.CUSTOM_PATH_ACCOUNTS)
                )
                val server = CellsApp.instance.accountService.sessionFactory.getServer(accountID)
                if (server.isLegacy) {
                    val toAuthIntent = Intent(requireActivity(), AuthActivity::class.java)
                    toAuthIntent.putExtra(AppNames.EXTRA_SERVER_URL, server.serverURL.toJson())
                    toAuthIntent.putExtra(AppNames.EXTRA_SERVER_IS_LEGACY, server.isLegacy)
                    toAuthIntent.putExtra(
                        AppNames.EXTRA_AFTER_AUTH_ACTION,
                        AuthService.NEXT_ACTION_ACCOUNTS
                    )
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
            AppNames.ACTION_LOGOUT -> lifecycleScope.launch {
                CellsApp.instance.accountService.logoutAccount(accountID)
            }
            AppNames.ACTION_FORGET -> {
                deleteAccount(requireContext(), accountID)
            }
            AppNames.ACTION_OPEN -> lifecycleScope.launch {
                CellsApp.instance.accountService.openSession(accountID)
                CellsApp.instance.setCurrentState(StateID.fromId(accountID))
                findNavController().navigate(MainNavDirections.openWorkspaces())
            }
            else -> return // do nothing
        }
    }
}
