package org.sinou.android.pydia.auth

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
import org.sinou.android.pydia.databinding.FragmentOauthFlowBinding

/** Manages the external OAuth process to get a JWT Token */
class OAuthFlowFragment : Fragment() {

    private val fTag = "OAuthFlowFragment"

    private lateinit var binding: FragmentOauthFlowBinding
    private lateinit var viewModelFactory: OAuthViewModel.OAuthFlowViewModelFactory
    private lateinit var viewModel: OAuthViewModel

    private val flowArgs by navArgs<OAuthFlowFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_oauth_flow, container, false
        )

        viewModelFactory = OAuthViewModel.OAuthFlowViewModelFactory(
            CellsApp.instance.accountService
        )
        val tmp: OAuthViewModel by viewModels { viewModelFactory }
        viewModel = tmp
        binding.oAuthViewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.launchOAuthIntent.observe(requireActivity(), { intent ->
            Log.i(fTag, "... ReadyToAuth")
            intent?.let {
                startActivity(intent)
                viewModel.authLaunched()
            }
        })

        viewModel.accountID.observe(requireActivity(), { accountId ->
            accountId?.let {
                var nextState = CellsApp.instance.getCurrentState()
                if (AppNames.CUSTOM_PATH_ACCOUNTS != nextState?.path) {
                    nextState = StateID.fromId(accountId)
                    CellsApp.instance.setCurrentState(nextState)
                }
                Log.i(fTag, "Auth Successful, navigating to $nextState")
                requireActivity().finish()
                startActivity(Intent(requireActivity(), MainActivity::class.java))
            }
        })

        binding.actionButton.setOnClickListener { Log.i(fTag, "TODO implement: cancel action...") }

        return binding.root
    }

    override fun onResume() {
        Log.i(fTag, "onResume")
        super.onResume()

        if (flowArgs.serverUrlString != null && !(viewModel.isProcessing.value!!)) {
            viewModel.launchOAuthProcess(
                ServerURLImpl.fromJson(flowArgs.serverUrlString)
            )
        } else {
            val uri = requireActivity().intent.data ?: return
            val state = uri.getQueryParameter(AppNames.KEY_STATE)
            val code = uri.getQueryParameter(AppNames.KEY_CODE)
            if (code != null && state != null) {
                viewModel.handleResponse(state, code)
            }
        }
    }

    override fun onPause() {
        Log.i(fTag, "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.i(fTag, "onStop")
        super.onStop()
    }

}
