package org.sinou.android.pydia.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentAccountListBinding
import org.sinou.android.pydia.room.account.AccountDB

class AccountListFragment : Fragment() {

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
        val viewModelFactory = AccountListViewModelFactory(dataSource, application)

        val accountListViewModel: AccountListViewModel by viewModels { viewModelFactory }

        val adapter = AccountListAdapter()
        binding.accountList.adapter = adapter

        accountListViewModel.accounts.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        binding.fab.setOnClickListener {
            it.findNavController().navigate(R.id.server_url_destination)
        }

        setHasOptionsMenu(true)
        return binding.root
    }
}
