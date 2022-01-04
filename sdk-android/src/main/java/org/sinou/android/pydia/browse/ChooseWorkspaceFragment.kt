package org.sinou.android.pydia.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentChooseWorkspaceBinding

class ChooseWorkspaceFragment : Fragment() {

    private val TAG = "ChooseWorkspaceFragment"

    private lateinit var accountID: String
    private lateinit var sessionVM: ForegroundSessionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentChooseWorkspaceBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_choose_workspace, container, false
        )

        requireActivity().intent.extras?.let {
            it[AppNames.EXTRA_STATE]?.let {
                if (it is String) {
                    accountID = StateID.fromId(it).accountId
                }
            }
        }

        val application = requireActivity().application
        val viewModelFactory = ForegroundSessionViewModel.ForegroundSessionViewModelFactory(
            CellsApp.instance.accountService,
            CellsApp.instance.nodeService,
            accountID,
            application,
        )

        val tmpVM: ForegroundSessionViewModel by viewModels { viewModelFactory }
        sessionVM = tmpVM

        val adapter = WsListAdapter { slug, action -> onWsClicked(slug, action) }
        binding.workspaces.adapter = adapter
        sessionVM.liveSession.observe(viewLifecycleOwner, Observer {
            it?.let {
                val currWss = it.workspaces ?: listOf()
                adapter.data = currWss.sorted()
            }
        })

        return binding.root
    }

    private fun onWsClicked(slug: String, action: String) {
        Log.i(TAG, "ID: $slug, do $action")

        when (action) {
//            "forget" -> lifecycleScope.launch {
//                CellsApp.instance.accountService.forgetAccount(slug)
//            }
            else -> return;// do nothing
        }
        // Toast.makeText(requireActivity(), "pos: $accountID, action ID: $action", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        sessionVM.resume()
    }

    override fun onPause() {
        super.onPause()
        sessionVM.pause()
    }

}
