package org.sinou.android.pydia.ui.auth

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentServerUrlBinding
import org.sinou.android.pydia.services.AuthService
import org.sinou.android.pydia.utils.hideKeyboard
import org.sinou.android.pydia.utils.showLongMessage

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

        viewModel.server.observe(viewLifecycleOwner) { server ->
            Log.i(fTag, "... LaunchingAuth")
            server?.let {
                val urlStr = server.serverURL.toJson()
                if (it.isLegacy) { // Navigate to in app legacy auth
                    val action = ServerUrlFragmentDirections.actionServerUrlToP8Creds(
                        urlStr,
                        AuthService.NEXT_ACTION_BROWSE
                    )
                    findNavController().navigate(action)
                    viewModel.authLaunched()
                } else { // Launch OAuth Process
                    viewModel.launchOAuthProcess(it.serverURL)
                }
            }
        }

        viewModel.unvalidTLS.observe(viewLifecycleOwner) { invalidTLS ->
            if (invalidTLS) {
                findNavController().navigate(ServerUrlFragmentDirections.actionConfirmSkipVerify())
            }
        }

        viewModel.nextIntent.observe(viewLifecycleOwner) { intent ->
            intent?.let {
                startActivity(intent)
                viewModel.intentStarted()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingIndicator.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let { showLongMessage(this@ServerUrlFragment.requireContext(), msg) }
        }

        return binding.root
    }

    private fun goForPing() {
        viewModel.pingAddress(binding.urlEditText.text.toString())
        binding.apply {
            // Update model?
            // Important: trigger re-paint Really ?
            invalidateAll()
        }
        hideKeyboard()
    }
}

class ConfirmSkipTlsVerificationDialog : DialogFragment() {

    private val viewModel: ServerUrlViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(R.string.confirm_skip_verify_title)
            .setMessage(R.string.confirm_skip_verify_desc)
            .setPositiveButton(R.string.button_i_understand_the_risks) { _, _ ->
                Log.i("ConfirmSkip", "Current stored address: ${viewModel.serverAddress.value}")
                viewModel.confirmTlsValidationSkip(true)
            }
            .setNegativeButton(R.string.button_cancel) { _, _ ->
                viewModel.confirmTlsValidationSkip(false)
            }
        return builder.create()
    }
}
