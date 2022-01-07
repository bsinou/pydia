package org.sinou.android.pydia.browse

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.pydio.cells.api.ui.WorkspaceNode
import org.sinou.android.pydia.R
import org.sinou.android.pydia.room.browse.RTreeNode

@BindingAdapter("nodeTitle")
fun TextView.setNodeTitle(item: RTreeNode?) {
    item?.let {
        text = item.name
    }
}

@BindingAdapter("nodeDesc")
fun TextView.setNodeDesc(item: RTreeNode?) {
    item?.let {
        text = item.parentPath
    }
}

@BindingAdapter("nodeThumb")
fun ImageView.setNodeThumb(item: RTreeNode) {
    setImageResource(
        when (item.type) {
            "todo" -> R.drawable.ic_baseline_folder_24
            else -> R.drawable.ic_baseline_insert_drive_file_24
        }
    )
}

@BindingAdapter("wsTitle")
fun TextView.setWsTitle(item: WorkspaceNode?) {
    item?.let {
        text = item.label
    }
}

@BindingAdapter("wsDesc")
fun TextView.setWsDesc(item: WorkspaceNode?) {
    item?.let {
        text = item.description
    }
}

@BindingAdapter("wsThumb")
fun ImageView.setWsThumb(item: WorkspaceNode) {
    setImageResource(
        when (item.type) {
            else -> R.drawable.ic_baseline_folder_24
        }
    )
}