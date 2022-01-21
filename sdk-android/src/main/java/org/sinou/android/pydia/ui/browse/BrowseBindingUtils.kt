package org.sinou.android.pydia.ui.browse

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE
import android.text.format.Formatter.formatShortFileSize
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.switchmaterial.SwitchMaterial
import com.pydio.cells.api.SdkNames
import com.pydio.cells.api.ui.WorkspaceNode
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import org.sinou.android.pydia.R
import org.sinou.android.pydia.db.browse.RTreeNode
import java.io.File

@BindingAdapter("nodeTitle")
fun TextView.setNodeTitle(item: RTreeNode?) {
    item?.let {
        text = if (SdkNames.NODE_MIME_RECYCLE == item.mime) {
            this.resources.getString(R.string.recycle_bin_label)
        } else {
            item.name
        }
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("nodeDesc")
fun TextView.setNodeDesc(item: RTreeNode?) {

    if (item == null) {
        return
    }

    val mTimeValue = DateUtils.formatDateTime(
        this.context,
        item.remoteModificationTS * 1000L,
        FORMAT_ABBREV_RELATIVE
    )
    val sizeValue = formatShortFileSize(this.context, item.size)
    text = "$mTimeValue • $sizeValue"
}

@BindingAdapter("nodeThumb")
fun ImageView.setNodeThumb(item: RTreeNode?) {

    if (item == null) {
        setImageResource(R.drawable.icon_file)
        return
    }

    if (Str.notEmpty(item.thumbFilename)) {
        val stat = StateID.fromId(item.encodedState)
        val path = "${this.context.filesDir.absolutePath}/" +
                "${stat.accountId}/thumbs/${item.thumbFilename}"

        Glide.with(this.context).load(File(path))
            // .transform(MultiTransformation(CenterCrop(), RoundedCorners(R.dimen.list_icon_corner_radius)))
            // TODO pass a radius in from resource in dp
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(16)))
            .into(this)
    } else {

        // TODO enrich with more specific icons for files depending on the mime
        setImageResource(
            when (item.mime) {
                SdkNames.NODE_MIME_FOLDER -> R.drawable.icon_folder
                SdkNames.NODE_MIME_RECYCLE -> R.drawable.icon_recycle
                else -> R.drawable.icon_file
            }
        )
    }
}

@BindingAdapter("offline")
fun ImageView.isOffline(item: RTreeNode) {
    visibility = if (item.isOfflineRoot) View.VISIBLE else View.GONE
}

@BindingAdapter("bookmark")
fun ImageView.isBookmark(item: RTreeNode) {
    visibility = if (item.isBookmarked) View.VISIBLE else View.GONE
}

@BindingAdapter("shared")
fun ImageView.isShared(item: RTreeNode) {
    visibility = if (item.isShared) View.VISIBLE else View.GONE
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
        getIconForWorkspace(item)
    )
}

fun getIconForWorkspace(item: WorkspaceNode) = when (item.workspaceType) {
    SdkNames.WS_TYPE_PERSONAL -> R.drawable.ic_baseline_folder_shared_24
    SdkNames.WS_TYPE_CELL -> R.drawable.cells
    else -> R.drawable.ic_baseline_folder_24
}

@BindingAdapter("hasPublicLink")
fun SwitchMaterial.setHasPublicLink(item: RTreeNode?) {
    item?.let {
        isChecked = it.isShared
    }
}

@BindingAdapter("isOfflineRoot")
fun SwitchMaterial.setOfflineRoot(item: RTreeNode?) {
    item?.let {
        isChecked = it.isOfflineRoot
    }
}

@BindingAdapter("isBookmarked")
fun SwitchMaterial.setBookmarked(item: RTreeNode?) {
    item?.let {
        isChecked = it.isBookmarked
    }
}

@BindingAdapter("showForFileOnly")
fun View.setShowForFileOnly(item: RTreeNode?) {
    item?.let {
        visibility = if (it.isFolder()) View.GONE else View.VISIBLE
    }
}

@BindingAdapter("showForFolderOnly")
fun View.setShowForFolderOnly(item: RTreeNode?) {
    item?.let {
        visibility = if (it.isFolder()) View.VISIBLE else View.GONE
    }
}

fun RTreeNode.isFolder(): Boolean {
    return SdkNames.NODE_MIME_FOLDER == mime || SdkNames.NODE_MIME_RECYCLE == mime
}

