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

    private val fTag = SortMenuFragment::class.java.simpleName

    private val oldOrder = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER)
    private val oldDirection = CellsApp.instance.getPreference(AppNames.PREF_KEY_CURR_RECYCLER_ORDER_DIR)

    private lateinit var sortBinding: MoreMenuSortBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(fTag, "onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sortBinding = DataBindingUtil.inflate(
            inflater, R.layout.more_menu_sort, container, false
        )
        configItem(sortBinding.byDefault, AppNames.SORT_BY_CANON, AppNames.SORT_BY_ASC)
        configItem(sortBinding.byNameAsc, AppNames.SORT_BY_NAME, AppNames.SORT_BY_ASC)
        configItem(sortBinding.byNameDesc, AppNames.SORT_BY_NAME, AppNames.SORT_BY_DESC)
        configItem(sortBinding.byRemoteTsDesc, AppNames.SORT_BY_REMOTE_TS, AppNames.SORT_BY_DESC)
        configItem(sortBinding.byRemoteTsAsc, AppNames.SORT_BY_REMOTE_TS, AppNames.SORT_BY_ASC)
        configItem(sortBinding.byMimeAsc, AppNames.SORT_BY_MIME, AppNames.SORT_BY_ASC)
        configItem(sortBinding.byMimeDesc, AppNames.SORT_BY_MIME, AppNames.SORT_BY_DESC)
        configItem(sortBinding.bySizeAsc, AppNames.SORT_BY_SIZE, AppNames.SORT_BY_ASC)
        configItem(sortBinding.bySizeDesc, AppNames.SORT_BY_SIZE, AppNames.SORT_BY_DESC)
        sortBinding.executePendingBindings()
        return sortBinding.root
    }

    private fun configItem(view: TextView, order: String, direction: String) {
        view.isActivated = oldOrder == order && oldDirection == direction
        requireActivity().theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view.isActivated) {
                view.setBackgroundColor(requireActivity().getColor(R.color.cells_main_light))
            } else {
                view.setBackgroundColor(requireActivity().getColor(R.color.transparent))
            }
        }
        view.setOnClickListener { onClicked(order, direction) }
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
}
