package org.sinou.android.pydia.ui.upload

import android.text.format.DateUtils
import android.text.format.Formatter
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.sinou.android.pydia.db.runtime.RUpload

@BindingAdapter("transferText")
fun TextView.setTransferText(item: RUpload?) {
    item?.let {
        val state = item.getStateId()
        text = "${state.fileName} (${state.username}@${state.serverHost})"
    }
}

@BindingAdapter("transferStatus")
fun TextView.setTransferStatus(item: RUpload?) {
    item?.let {

        val sizeValue = Formatter.formatShortFileSize(this.context, item.byteSize)
        var desc = "$sizeValue,"

        if (item.doneTimestamp > 0) {

            val mTimeValue = DateUtils.formatDateTime(
                this.context,
                item.doneTimestamp * 1000L,
                DateUtils.FORMAT_ABBREV_RELATIVE
            )
            desc += "uploaded on $mTimeValue"
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
            desc += "started on $mTimeValue"
        }
        text = desc

    }
}

@BindingAdapter("updateProgress")
fun ProgressBar.setUpdateProgress(item: RUpload?) {
    item?.let {
        // TODO compute from total size
        progress = it.progress
    }
}
