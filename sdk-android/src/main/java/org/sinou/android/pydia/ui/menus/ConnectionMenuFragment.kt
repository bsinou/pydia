package org.sinou.android.pydia.ui.menus

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.MoreMenuSortBinding


class ConnectionMenuFragment : BottomSheetDialogFragment() {

    private val fTag = ConnectionMenuFragment::class.java.simpleName

    val oldOrder = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER)
    val oldDirection = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER_DIR)

    private lateinit var sortBinding: MoreMenuSortBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(fTag, "onCreate")

        val application = requireActivity().application
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        sortBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_sort, container, false
        )
        sortBinding.byNameAsc.setOnClickListener { onClicked(SORT_BY_NAME, SORT_BY_ASC) }
        sortBinding.executePendingBindings()

        return sortBinding.root
    }

    override fun onResume() {
        super.onResume()
        Log.i(fTag, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(fTag, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(fTag, "onStop")
    }


    private fun bind(binding: MoreMenuSortBinding) {
        // binding.node = node
//         binding.openWith.setOnClickListener { onClicked(node, ACTION_OPEN_WITH) }

    }

    private fun onClicked(order: String, direction: String) {
        // val moreMenu = this
        CellsApp.instance.setPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER, order)
    }

}
