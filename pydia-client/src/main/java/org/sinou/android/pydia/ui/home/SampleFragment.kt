package org.sinou.android.pydia.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.sinou.android.pydia.databinding.ZzSampleWidgetsBinding

class SampleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = ZzSampleWidgetsBinding.inflate(inflater, container, false)
        return binding.root
    }
}
