package org.sinou.android.pydia.ui.bindings

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.R
import org.sinou.android.pydia.db.runtime.RTransfer

@SuppressLint("SetTextI18n")
@BindingAdapter("transferText")
fun TextView.setTransferText(item: RTransfer?) {
    item?.let {
        val state = item.getStateId()
        text = "${state.fileName} -> ${state.username}@${state.serverHost}"
    }
}

@BindingAdapter("transferIcon")
fun ImageView.setTransferIcon(item: RTransfer?) {
    if (item == null) {
        return
    }
    setImageResource(
        when (item.type) {
            AppNames.TRANSFER_TYPE_DOWNLOAD -> R.drawable.ic_baseline_cloud_download_24
            else -> R.drawable.ic_baseline_cloud_upload_24
        }
    )
}

@BindingAdapter("transferStatus")
fun TextView.setTransferStatus(item: RTransfer?) {
    item?.let {

        val sizeValue = Formatter.formatShortFileSize(this.context, item.byteSize)
        var desc = "$sizeValue,"
        // TODO handle error
        if (item.doneTimestamp > 0) {

            val mTimeValue = DateUtils.formatDateTime(
                this.context,
                item.doneTimestamp * 1000L,
                DateUtils.FORMAT_ABBREV_RELATIVE
            )
            desc += " uploaded on $mTimeValue"
        } else {

            var ts = item.startTimestamp
            if (ts < 0) {
                ts = 0
            }

            val mTimeValue = DateUtils.formatDateTime(
                this.context,
                ts * 1000L,
                DateUtils.FORMAT_ABBREV_RELATIVE
            )
            desc += " started on $mTimeValue"
        }
        text = desc

    }
}

@BindingAdapter("updateProgress")
fun ProgressBar.setUpdateProgress(item: RTransfer?) {
    item?.let {
        val percentage = (it.progress * 100) / it.byteSize
        Log.e("Progress", "${it.progress} - ${it.byteSize} - $percentage")
        progress = percentage.toInt()
    }
}

@BindingAdapter("showForFailedOnly")
fun View.showForFailedOnly(item: RTransfer?) {
    item?.let {
        visibility = if (Str.notEmpty(item.error)) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}

@BindingAdapter("showForDoneOnly")
fun View.showForDoneOnly(item: RTransfer?) {
    item?.let {
        visibility = if (it.doneTimestamp > 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}

@BindingAdapter("parentPrimaryText")
fun TextView.setParentPrimaryText(parentState: StateID?) {
    parentState?.let {
        text = when {
            parentState.id == AppNames.CELLS_ROOT_ENCODED_STATE -> this.resources.getString(R.string.switch_account)
            Str.empty(parentState.workspace) -> this.resources.getString(R.string.switch_workspace)
            else -> ".."
        }
    }
}

@BindingAdapter("parentSecondaryText")
fun TextView.setParentSecondaryText(parentState: StateID?) {
    Log.e("setParentSecondaryText", "ParentState: $parentState")
    parentState?.let {
        text = when {
            parentState.id == AppNames.CELLS_ROOT_ENCODED_STATE -> ""
            Str.empty(parentState.workspace) -> parentState.serverHost
            else -> this.resources.getString(R.string.parent_folder)
        }
    }
}
