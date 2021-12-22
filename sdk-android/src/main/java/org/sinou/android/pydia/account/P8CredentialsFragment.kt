package org.sinou.android.pydia.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainActivity
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentP8CredentialsBinding

/** Handle user filled credentials for P8 remote servers */
class P8CredentialsFragment : Fragment() {

    private val TAG = "P8CredentialsFragment"

    private val viewModelFactory = ServerUrlViewModel.ServerUrlViewModelFactory(CellsApp.instance.accountService)
    private val viewModel: ServerUrlViewModel by activityViewModels { viewModelFactory }

    private lateinit var binding: FragmentP8CredentialsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_p8_credentials, container, false
        )

        binding.actionButton.setOnClickListener { launchAuth(it) }

        viewModel.accountID.observe(requireActivity(), Observer { accountId ->
            Log.i(TAG, "... Got an account navigating to browse activity")
            accountId?.let {
                val toBrowseIntent = Intent(requireActivity(), MainActivity::class.java)
                toBrowseIntent.putExtra(AppNames.EXTRA_STATE, accountId)
                startActivity(toBrowseIntent);
            }
        })

        return binding.root
    }

    private fun launchAuth(v: View) {
        viewModel.logToP8(
            binding.loginEditText.text.toString(),
            binding.passwordEditText.text.toString(),
            null
        )
    }
}
