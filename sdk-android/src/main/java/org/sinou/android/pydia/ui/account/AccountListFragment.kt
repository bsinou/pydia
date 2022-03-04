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
import com.pydio.cells.transport.ServerURLImpl
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.*
import org.sinou.android.pydia.databinding.FragmentAccountListBinding
import org.sinou.android.pydia.services.AuthService
import org.sinou.android.pydia.ui.common.deleteAccount
import org.sinou.android.pydia.ui.common.logoutAccount

class AccountListFragment : Fragment() {

    private val fTag = AccountListFragment::class.java.simpleName

    private lateinit var binding: FragmentAccountListBinding
    private lateinit var accountListViewModel: AccountListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e(fTag, "onCreateView ${savedInstanceState?.getString(AppNames.KEY_STATE)}")
        // Get a reference to the binding object and inflate the fragment views.
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_account_list, container, false
        )

        val application = requireNotNull(this.activity).application
        val viewModelFactory = AccountListViewModel.AccountListViewModelFactory(
            CellsApp.instance.accountService,
            application
        )
        val tmp: AccountListViewModel by viewModels { viewModelFactory }
        accountListViewModel = tmp

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
//                CellsApp.instance.setCurrentState(
//                    StateID.fromId(accountID).withPath(AppNames.CUSTOM_PATH_ACCOUNTS)
//                )

                val currSession = accountListViewModel.sessions.value
                    ?.filter { it.accountID == accountID }
                    ?.get(0)
                if (currSession == null) {
                    Log.i(fTag, "No live session found for: $accountID in ViewModel, aborting...")
                    return
                }
                // FIXME clean this when implementing custom certificate acceptance.
                val serverURL = ServerURLImpl.fromAddress(currSession.url, currSession.tlsMode == 1)

                if (currSession.isLegacy) {
                    val toAuthIntent = Intent(requireActivity(), AuthActivity::class.java)

                    toAuthIntent.putExtra(AppNames.EXTRA_SERVER_URL, serverURL.toJson())
                    toAuthIntent.putExtra(AppNames.EXTRA_SERVER_IS_LEGACY, true)
                    toAuthIntent.putExtra(
                        AppNames.EXTRA_AFTER_AUTH_ACTION,
                        AuthService.NEXT_ACTION_ACCOUNTS
                    )
                    startActivity(toAuthIntent)
                } else {
                    lifecycleScope.launch {
                        val toAuthIntent =
                            accountListViewModel.accountService.authService.createOAuthIntent(
                                accountListViewModel.accountService,
                                serverURL,
                                AuthService.NEXT_ACTION_ACCOUNTS
                            )

                        if (toAuthIntent == null){
                            Log.e(fTag, "Could not create OAuth intent for ${serverURL.url}")
                            return@launch
                        }
                        startActivity(toAuthIntent)
                    }
                }
            }
            AppNames.ACTION_LOGOUT -> lifecycleScope.launch {
                logoutAccount(requireContext(), accountID)
            }
            AppNames.ACTION_FORGET -> {
                deleteAccount(requireContext(), accountID)
            }
            AppNames.ACTION_OPEN -> lifecycleScope.launch {
                CellsApp.instance.accountService.openSession(accountID)
                CellsApp.instance.setCurrentState(StateID.fromId(accountID))

                // findNavController().navigate(MainNavDirections.openWorkspaces())
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.putExtra(AppNames.EXTRA_STATE, accountID)
                startActivity(intent)
            }
            else -> return // do nothing
        }
    }
}
