package org.sinou.android.pydia.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.pydio.cells.transport.ServerURLImpl
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainActivity
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentP8CredentialsBinding

/** Handle user filled credentials for P8 remote servers */
class P8CredentialsFragment : Fragment() {

    companion object {
        private const val TAG = "P8CredentialsFragment"
    }

    private lateinit var viewModelFactory: P8CredViewModel.P8CredViewModelFactory
    private lateinit var viewModel: P8CredViewModel

    private lateinit var binding: FragmentP8CredentialsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_p8_credentials, container, false
        )

        val credArgs by navArgs<P8CredentialsFragmentArgs>()
        viewModelFactory = P8CredViewModel.P8CredViewModelFactory(
            CellsApp.instance.accountService,
            ServerURLImpl.fromJson(credArgs.serverUrlString)
        )

        val tmp: P8CredViewModel by viewModels { viewModelFactory }
        viewModel = tmp
        binding.p8CredViewModel = viewModel
        binding.lifecycleOwner = this

        binding.actionButton.setOnClickListener { launchAuth() }

        viewModel.accountID.observe(requireActivity()) { accountId ->
            accountId?.let {
                var nextState = CellsApp.instance.getCurrentState()
                if (AppNames.CUSTOM_PATH_ACCOUNTS != nextState?.path) {
                    nextState = StateID.fromId(accountId)
                    CellsApp.instance.setCurrentState(nextState)
                }
                Log.i(TAG, "Auth Successful, navigating to $nextState")
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            }
        }

        // TODO handle captcha
        return binding.root
    }

    private fun launchAuth() {
        viewModel.logToP8(
            binding.loginEditText.text.toString(),
            binding.passwordEditText.text.toString(),
            null
        )
    }
}
