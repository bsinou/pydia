package org.sinou.android.pydia.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.sinou.android.pydia.R
import org.sinou.android.pydia.account.AccountListViewModel
import org.sinou.android.pydia.account.AccountListViewModelFactory
import org.sinou.android.pydia.databinding.FragmentAccountListBinding
import org.sinou.android.pydia.databinding.FragmentBrowseListBinding
import org.sinou.android.pydia.room.account.AccountDatabase

class ChooseWorkspaceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentBrowseListBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_browse_list, container, false
        )

        return binding.root
    }
}
