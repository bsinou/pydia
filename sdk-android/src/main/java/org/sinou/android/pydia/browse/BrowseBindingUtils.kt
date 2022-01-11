package org.sinou.android.pydia.browse

import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE
import android.text.format.Formatter.formatShortFileSize
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.pydio.cells.api.SdkNames
import com.pydio.cells.api.ui.WorkspaceNode
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import org.sinou.android.pydia.R
import org.sinou.android.pydia.room.browse.RTreeNode
import java.io.File

@BindingAdapter("nodeTitle")
fun TextView.setNodeTitle(item: RTreeNode?) {
    item?.let {
        text = item.name
    }
}

@BindingAdapter("nodeDesc")
fun TextView.setNodeDesc(item: RTreeNode?) {

    if (item == null) {
        return
    }

    var mTimeValue = DateUtils.formatDateTime(
        this.context,
        item.remoteModificationTS * 1000L,
        FORMAT_ABBREV_RELATIVE
    )
    val sizeValue = formatShortFileSize(this.context, item.size)
    text = " ${mTimeValue} • ${sizeValue}"
}

@BindingAdapter("nodeThumb")
fun ImageView.setNodeThumb(item: RTreeNode) {

    if (Str.notEmpty(item.thumbFilename)) {
        val stat = StateID.fromId(item.encodedState)
        val path = "/data/data/org.sinou.android.pydia/files/" +
                "${stat.accountId}/thumbs/${item.thumbFilename}"
        Log.w("BA.SetThumb", "About to load: $path")
        Glide.with(this.context).load(File(path)).into(this)
    } else {
        setImageResource(
            when (item.mime) {
                SdkNames.NODE_MIME_FOLDER -> R.drawable.ic_baseline_folder_24
                SdkNames.NODE_MIME_RECYCLE -> R.drawable.ic_baseline_folder_delete_24
                else -> R.drawable.ic_baseline_insert_drive_file_24
            }
        )
    }
}

@BindingAdapter("offline")
fun ImageView.isOffline(item: RTreeNode) {
    item?.let {
        visibility = if (it.isOfflineRoot) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("bookmark")
fun ImageView.isBookmark(item: RTreeNode) {
    item?.let {
        visibility = if (it.isBookmarked) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("shared")
fun ImageView.isShared(item: RTreeNode) {
    item?.let {
        visibility = if (it.isShared) View.VISIBLE else View.GONE
    }
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