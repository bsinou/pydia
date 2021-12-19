package org.sinou.android.pydia.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
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

    private val TAG = "ServerUrlFragment"

    val viewModelFactory = ServerUrlViewModelFactory(CellsApp.instance.accountRepository)
    private val viewModel: ServerUrlViewModel by activityViewModels { viewModelFactory }

    private lateinit var binding: FragmentServerUrlBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_server_url, container, false
        )

        binding.actionButton.setOnClickListener { goForPing(it) }

        viewModel.launchOAuthIntent.observe(requireActivity(), Observer { intent ->
            Log.i(TAG, "... ReadyToAuth")
            intent?.let {
                startActivity(intent)
                viewModel.authLaunched()
            }
        })

        viewModel.server.observe(requireActivity(), Observer { server ->
            Log.i(TAG, "... LaunchingAuth")
            server?.let {
                if (it.isLegacy) { // Navigate to in app auth
                    Log.i(TAG, "... Legacy server => display p8cred fragment")
                    val action =
                        ServerUrlFragmentDirections.actionServerUrlFragmentToP8CredentialsFragment()
                    binding.serverUrlFragment.findNavController().navigate(action)
                } else { // Launch OAuth Process
                    Log.i(TAG, "... Cells server => launch OAuth flow")
                    viewModel.launchOAuthProcess(server)
                }
                viewModel.authLaunched()
            }
        })

        viewModel.errorMessage.observe(requireActivity(), Observer { msg ->
            msg?.let {
                Toast.makeText(requireActivity().application, msg, Toast.LENGTH_LONG).show()
            }
        })

        return binding.root
    }

    fun goForPing(v: View) {

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
