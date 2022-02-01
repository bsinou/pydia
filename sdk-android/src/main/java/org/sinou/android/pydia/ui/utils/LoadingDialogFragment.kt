package org.sinou.android.pydia.ui.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.sinou.android.pydia.R

class LoadingDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_progress, container)
    }

    companion object {
        fun newInstance(): LoadingDialogFragment {
            val frag = LoadingDialogFragment()
            val args = Bundle()
            frag.setArguments(args)
            return frag
        }
    }
}