package org.sinou.android.pydia.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentAboutBinding
import org.sinou.android.pydia.databinding.FragmentHelloBinding

class HelloFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val binding: FragmentHelloBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_hello, container, false
        )

        setHasOptionsMenu(true)

        return binding.root
    }
}
