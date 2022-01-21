package org.sinou.android.pydia.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentServerUrlBinding
import org.sinou.android.pydia.utils.hideKeyboard

/**
 * Handle the registration of a new Server URL, it manages:
 *  - URL validation
 *  - TLS check
 *  - Skip verify flag
 */
class ServerUrlFragment : Fragment() {

    private val fTag = "ServerUrlFragment"

    private lateinit var binding: FragmentServerUrlBinding
    private val viewModelFactory =
        ServerUrlViewModel.ServerUrlViewModelFactory(CellsApp.instance.accountService)
    private val viewModel: ServerUrlViewModel by activityViewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_server_url, container, false
        )

        binding.actionButton.setOnClickListener { goForPing() }

        viewModel.server.observe(requireActivity(), { server ->
            Log.i(fTag, "... LaunchingAuth")
            server?.let {
                val urlStr = server.serverURL.toJson()
                if (it.isLegacy) { // Navigate to in app auth
                    Log.i(fTag, "... Legacy server => display p8cred fragment")
                    val action = ServerUrlFragmentDirections.actionServerUrlToP8Creds(urlStr)
                    findNavController().navigate(action)
                } else { // Launch OAuth Process
                    Log.i(fTag, "... Cells server => launch OAuth flow")
                    val action = ServerUrlFragmentDirections.actionServerUrlToOauthFlow(urlStr)
                    findNavController().navigate(action)
                    // viewModel.launchOAuthProcess(server)
                }
                viewModel.authLaunched()
            }
        })

        viewModel.errorMessage.observe(requireActivity(), { msg ->
            msg?.let {
                Toast.makeText(requireActivity().application, msg, Toast.LENGTH_LONG).show()
            }
        })

        return binding.root
    }

    private fun goForPing() {

        viewModel.pingAddress(binding.urlEditText.text.toString())
        binding.apply {
            // Update model?
            // Important: trigger re-paint Really ?
            invalidateAll()
            // Explicitly refresh UI ?
        }

        hideKeyboard()
    }
}
