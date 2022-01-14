package org.sinou.android.pydia.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.sinou.android.pydia.R

class TreeNodeMoreMenu : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.modal_tree_node_more_menu, container, false)

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}