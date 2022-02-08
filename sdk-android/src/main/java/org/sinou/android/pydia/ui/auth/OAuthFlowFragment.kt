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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.MainActivity
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentOauthFlowBinding
import org.sinou.android.pydia.services.AuthService

/** Manages the external OAuth process to get a JWT Token */
class OAuthFlowFragment : Fragment() {

    private val fTag = "OAuthFlowFragment"

    private lateinit var binding: FragmentOauthFlowBinding
    private lateinit var viewModelFactory: OAuthViewModel.OAuthFlowViewModelFactory
    private lateinit var viewModel: OAuthViewModel
    private lateinit var navController: NavController

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
        // binding.lifecycleOwner = this
        navController = findNavController()

        viewModel.accountID.observe(requireActivity()) { pair ->
            pair?.let {
                val (accountID, next) = pair
                var nextState = CellsApp.instance.getCurrentState()
                when (next) {
                    AuthService.NEXT_ACTION_TERMINATE -> {} // Do nothing => we return where we launched the auth process
                    AuthService.NEXT_ACTION_ACCOUNTS -> {
                        // A priori, we come from the account list and return there, no need
                        // to change everything, put a log for the time being to be sure
                        Log.i(fTag, "Auth success, about to browse to $nextState")
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                    }
                    AuthService.NEXT_ACTION_BROWSE -> {
                        // We have registered a new account and want to browse to it
                        nextState = StateID.fromId(accountID)
                        CellsApp.instance.setCurrentState(nextState)
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        intent.putExtra(AppNames.EXTRA_STATE, accountID)
                        Log.i(fTag, "Auth Successful, navigating to $nextState")
                        startActivity(intent)
                    }
                }
                requireActivity().finish()
            }
        }

        binding.actionButton.setOnClickListener { navController.navigateUp() }

        return binding.root
    }

    override fun onResume() {
        Log.i(fTag, "onResume")
        super.onResume()
        val uri = requireActivity().intent.data ?: return
        val state = uri.getQueryParameter(AppNames.KEY_STATE)
        val code = uri.getQueryParameter(AppNames.KEY_CODE)
        if (code != null && state != null) {
            viewModel.handleResponse(state, code)
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
