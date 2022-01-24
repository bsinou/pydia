package org.sinou.android.pydia.ui.browse

import android.annotation.SuppressLint
import android.content.Context
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
        Glide.with(context).load(File(getPathForGlide(context, item)))
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(R.dimen.glide_thumb_radius)))
            .into(this)
    } else {
        setImageResource( getDrawableFromMime(item.mime)
        )
    }
}

fun getPathForGlide(context: Context, item: RTreeNode): String {
    val stat = StateID.fromId(item.encodedState)
    return "${context.filesDir.absolutePath}/" +
            "${stat.accountId}/thumbs/${item.thumbFilename}"
}


fun getDrawableFromMime(mime: String): Int {
    // TODO enrich with more specific icons for files depending on the mime
    return when (mime) {
        SdkNames.NODE_MIME_FOLDER -> R.drawable.icon_folder
        SdkNames.NODE_MIME_RECYCLE -> R.drawable.icon_recycle
        else -> R.drawable.icon_file
    }
}

@BindingAdapter("cardThumb")
fun ImageView.setCardThumb(item: RTreeNode?) {

    if (item == null) {
        setImageResource(R.drawable.icon_file)
        return
    }

    if (Str.notEmpty(item.thumbFilename)) {
        Glide.with(context).load(File(getPathForGlide(context, item)))
            .transform(CenterCrop()).into(this)
    } else {
        setImageResource( getDrawableFromMime(item.mime)
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

fun areContentsEquals(
    oldItem: RTreeNode,
    newItem: RTreeNode
): Boolean {
    var same = oldItem.remoteModificationTS == newItem.remoteModificationTS

    if (same && newItem.thumbFilename != null) {
        same = newItem.thumbFilename.equals(oldItem.thumbFilename)
    }

    val flagChanged = newItem.isBookmarked == oldItem.isBookmarked
            && newItem.isOfflineRoot == oldItem.isOfflineRoot
            && newItem.isShared == oldItem.isShared


    // With Room: we should get  equality based on equality of each fields (column) for free
    // (RTreeNode is a @Data class). But this doesn't work for now, so we rather only check:
    // remote modif timestamp and thumb filename.

    // More logs to investigate
    //        if (!same){
    //            Log.d(tag, "Found new content for ${oldItem.encodedState}")
    //            Log.d(tag, "Old TS: ${oldItem.remoteModificationTS}, " +
    //                    "new TS: ${newItem.remoteModificationTS}")
    //            Log.d(tag, "Old thumb: ${oldItem.thumbFilename}, " +
    //                    "new thumb: ${newItem.thumbFilename}")
    ////            Log.d(tag, "old item: \n${Gson().toJson(oldItem)}")
    ////            Log.d(tag, "new item: \n${Gson().toJson(newItem)}")
    //        }
    return same && flagChanged
}