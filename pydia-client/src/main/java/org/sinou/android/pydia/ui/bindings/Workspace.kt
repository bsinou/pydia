package org.sinou.android.pydia.ui.bindings

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.pydio.cells.api.SdkNames
import org.sinou.android.pydia.R
import org.sinou.android.pydia.db.accounts.RWorkspace
import java.util.*

@BindingAdapter("wsHeaderLabel")
fun TextView.setWsHeaderLabel(type: String?) {
    type?.let {
        val nodeType =
            when (it) {
                SdkNames.WS_TYPE_CELL -> this.resources.getString(R.string.category_cells)
                else -> this.resources.getString(R.string.category_workspaces)
            }
        text = nodeType.uppercase(Locale.getDefault())
    }
}

@BindingAdapter("wsTitle")
fun TextView.setWsTitle(item: RWorkspace?) {
    item?.let { text = item.label }
}

@BindingAdapter("wsDesc")
fun TextView.setWsDesc(item: RWorkspace?) {
    item?.let { text = item.description }
}

@BindingAdapter("wsThumb")
fun ImageView.setWsThumb(item: RWorkspace) {
    setImageResource(getIconForWorkspace(item))
}

fun getWsIconForMenu(item: RWorkspace) = when (item.type) {
    // TODO we hard code the tint in the XML Layout
    SdkNames.WS_TYPE_PERSONAL -> R.drawable.ic_baseline_folder_shared_24
    SdkNames.WS_TYPE_CELL -> R.drawable.cells
    else -> R.drawable.ic_baseline_folder_24
}

fun getIconForWorkspace(item: RWorkspace) = when (item.type) {
    SdkNames.WS_TYPE_PERSONAL -> R.drawable.icon_personal
    SdkNames.WS_TYPE_CELL -> R.drawable.icon_cell
    else -> R.drawable.icon_workspace
}