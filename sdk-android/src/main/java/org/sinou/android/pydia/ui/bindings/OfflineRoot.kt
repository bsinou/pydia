package org.sinou.android.pydia.ui.bindings

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE
import android.text.format.Formatter.formatShortFileSize
import android.util.Log
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
import com.pydio.cells.utils.Str
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.db.nodes.RLiveOfflineRoot
import org.sinou.android.pydia.db.nodes.RTreeNode
import java.io.File

@BindingAdapter("offlineRootTitle")
fun TextView.setOfflineRootTitle(item: RLiveOfflineRoot?) {
    item?.let {
        text = if (SdkNames.NODE_MIME_RECYCLE == item.mime) {
            this.resources.getString(R.string.recycle_bin_label)
        } else {
            item.name
        }
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("offlineRootDesc")
fun TextView.setOfflineRootDesc(item: RLiveOfflineRoot?) {

    if (item == null) {
        return
    }

    val mTimeValue = DateUtils.formatDateTime(
        this.context,
        item.lastCheckTs * 1000L,
        FORMAT_ABBREV_RELATIVE
    )
    val sizeValue = formatShortFileSize(this.context, item.size)
    text = "$mTimeValue • $sizeValue"
}

@BindingAdapter("isOffline")
fun SwitchMaterial.setIsOffline(item: RLiveOfflineRoot?) {
    item?.let { isChecked = true }
}

@BindingAdapter("showForOfflineFileOnly")
fun View.setShowForOfflineFileOnly(item: RLiveOfflineRoot?) {
    item?.let { visibility = if (it.isFolder()) View.GONE else View.VISIBLE }
}

@BindingAdapter("offlineRootThumb")
fun ImageView.setOfflineRootThumb(item: RLiveOfflineRoot?) {

    if (item == null) {
        setImageResource(R.drawable.icon_file)
        return
    }

//    if (item.localModificationTS > item.remoteModificationTS) {
//        setImageResource(R.drawable.loading_animation)
//        return
//    }

    val lf = CellsApp.instance.fileService.getOfflineThumbPath(item)
    if (Str.notEmpty(lf) && File(lf!!).exists()) {
        Glide.with(context)
            .load(File(lf))
            .transform(
                MultiTransformation(
                    CenterCrop(),
                    // TODO Directly getting  the radius with R fails => image is a circle
                    // RoundedCorners(R.dimen.glide_thumb_radius)
                    RoundedCorners(16)
                )
            )
            .into(this)
    } else {
        // Log.w("SetNodeThumb", "no thumb found for ${item.name}")
        setImageResource(getDrawableFromMime(item.mime, item.sortName))
    }
}

@BindingAdapter("offlineRootCardThumb")
fun ImageView.setOfflineRootCardThumb(item: RLiveOfflineRoot?) {
    if (item == null) {
        setImageResource(R.drawable.icon_file)
        return
    }

//    if (item.localModificationTS > item.remoteModificationTS) {
//        setImageResource(R.drawable.loading_animation2)
//        return
//    }

    val lf = CellsApp.instance.fileService.getOfflineThumbPath(item)
    if (Str.notEmpty(lf) && File(lf!!).exists()) {
        Glide.with(context)
            .load(File(lf))
            .transform(CenterCrop())
            .into(this)
    } else {
        Log.w("SetCardThumb", "no thumb found for ${item.name}")
        setImageResource(getDrawableFromMime(item.mime, item.sortName))
    }
}
