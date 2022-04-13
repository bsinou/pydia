package org.sinou.android.pydia.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentAccountDetailsBinding

class AccountDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentAccountDetailsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_account_details, container, false
        )

        return binding.root
    }
}
