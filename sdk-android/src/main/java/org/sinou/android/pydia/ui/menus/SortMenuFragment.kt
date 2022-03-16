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

const val SORT_BY_ORDER = "sort_by_order"
const val SORT_BY_ASC = "ASC"
const val SORT_BY_DESC = "DESC"

const val SORT_BY_NAME = "name"
const val SORT_BY_MIME = "mime"
const val SORT_BY_SIZE = "size"
const val SORT_BY_LAST_REMOTE_MODIFICATION = "remote_mod_ts"
const val SORT_BY_LAST_CHECK = "last_check_ts"

class SortMenuFragment : BottomSheetDialogFragment() {

    private val fTag = SortMenuFragment::class.java.simpleName

    val oldOrder = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER)
    val oldDirection = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER_DIR)

    private lateinit var sortBinding: MoreMenuSortBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(fTag, "onCreate")
        super.onCreate(savedInstanceState)
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
        sortBinding.byNameDesc.setOnClickListener { onClicked(SORT_BY_NAME, SORT_BY_DESC) }
        sortBinding.byRemoteTsDesc.setOnClickListener {
            onClicked(
                SORT_BY_LAST_REMOTE_MODIFICATION,
                SORT_BY_DESC
            )
        }
        sortBinding.byRemoteTsAsc.setOnClickListener {
            onClicked(
                SORT_BY_LAST_REMOTE_MODIFICATION,
                SORT_BY_ASC
            )
        }
        sortBinding.byMimeAsc.setOnClickListener { onClicked(SORT_BY_MIME, SORT_BY_ASC) }
        sortBinding.byMimeDesc.setOnClickListener { onClicked(SORT_BY_MIME, SORT_BY_DESC) }
        sortBinding.bySizeAsc.setOnClickListener { onClicked(SORT_BY_SIZE, SORT_BY_ASC) }
        sortBinding.bySizeDesc.setOnClickListener { onClicked(SORT_BY_SIZE, SORT_BY_DESC) }

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

    private fun onClicked(order: String, direction: String) {
        Log.i(tag, "Item clicked: ORDER BY $order $direction")
        if (oldOrder != order || oldDirection != direction) {
            CellsApp.instance.setPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER, order)
            CellsApp.instance.setPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER_DIR, direction)
            dismiss()
            requireActivity().recreate()
        } else {
            dismiss()
        }
    }
}
