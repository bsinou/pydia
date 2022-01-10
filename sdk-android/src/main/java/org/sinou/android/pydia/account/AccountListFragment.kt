package org.sinou.android.pydia.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import kotlinx.coroutines.*
import org.sinou.android.pydia.AuthActivity
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.browse.ForegroundSessionViewModel
import org.sinou.android.pydia.databinding.FragmentAccountListBinding
import org.sinou.android.pydia.room.account.AccountDB

class AccountListFragment : Fragment() {
    private val TAG = "AccountListFragment"

//    val job = Job()
//    val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentAccountListBinding = DataBindingUtil.inflate(
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

        accountListViewModel.sessions.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        // TODO implement color change on session switch
        // foregroundSessionViewModel.activeSession.observe()

        binding.fab.setOnClickListener {
            // it.findNavController().navigate(R.id.server_url_destination)
            val toBrowseIntent = Intent(requireActivity(), AuthActivity::class.java)
            startActivity(toBrowseIntent);
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun onAccountClicked(accountID: String, action: String) {
        Log.i(TAG, "ID: $accountID, do $action")

        when (action) {
            "forget" -> lifecycleScope.launch {
                CellsApp.instance.accountService.forgetAccount(accountID)
            }
            else -> return;// do nothing
        }
        // Toast.makeText(requireActivity(), "pos: $accountID, action ID: $action", Toast.LENGTH_LONG).show()
    }

}
