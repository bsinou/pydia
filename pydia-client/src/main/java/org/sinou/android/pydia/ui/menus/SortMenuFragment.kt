package org.sinou.android.pydia.ui.menus

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.MoreMenuSortBinding

/**
 * Simple bottom menu to manage the application wide sort order for various node lists.
 * Corresponding values (sort name and direction) are stored in the preferences.
 */
class SortMenuFragment : BottomSheetDialogFragment() {

    private val logTag = SortMenuFragment::class.java.simpleName

    private val oldOrder = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER)
    //private val oldDirection = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER_DIR)

    private lateinit var sortBinding: MoreMenuSortBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(logTag, "onCreateView")
        sortBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_sort, container, false
        )

// TODO make the text views dynamic
//        val keys = resources.getStringArray(R.array.order_by_values)
//        for ((i, label) in resources.getStringArray(R.array.order_by_labels).withIndex()){
//            configItem(sortBinding.byDefault, "${AppNames.SORT_BY_CANON}||${AppNames.SORT_BY_ASC}")
//
//        }

        val keys = resources.getStringArray(R.array.order_by_values)
        configItem(sortBinding.byDefault, keys[0])
        configItem(sortBinding.byNameAsc, keys[1])
        configItem(sortBinding.byNameDesc, keys[2])
        configItem(sortBinding.byRemoteTsDesc, keys[3])
        configItem(sortBinding.byRemoteTsAsc, keys[4])
        configItem(sortBinding.byMimeAsc, keys[5])
        configItem(sortBinding.byMimeDesc, keys[6])
        configItem(sortBinding.bySizeAsc, keys[7])
        configItem(sortBinding.bySizeDesc, keys[8])
        sortBinding.executePendingBindings()
        return sortBinding.root
    }

    private fun configItem(view: TextView, encodedOrder: String) {
        view.isActivated = oldOrder == encodedOrder // && oldDirection == direction
        requireActivity().theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view.isActivated) {
                view.setBackgroundColor(requireActivity().getColor(R.color.selected_background))
            } else {
                view.setBackgroundColor(requireActivity().getColor(R.color.transparent))
            }
        }
        view.setOnClickListener { onClicked(encodedOrder) }
    }

    private fun onClicked(order: String) {
        Log.d(tag, "Item clicked: ORDER BY $order ")
        if (oldOrder != order) {
            CellsApp.instance.setPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER, order)
            // CellsApp.instance.setPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER_DIR, direction)
            dismiss()
            requireActivity().recreate()
        } else {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(logTag, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(logTag, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(logTag, "onStop")
    }
}
