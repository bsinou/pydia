package org.sinou.android.pydia.ui.upload

import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.progressindicator.BaseProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.pydio.cells.api.ui.WorkspaceNode
import org.sinou.android.pydia.db.runtime.RUpload

@BindingAdapter("transferText")
fun TextView.setTransferText(item: RUpload?) {
    item?.let {
        text = "my-image-88639.jpg - 658KB - Started 2 years ago..."
    }
}

@BindingAdapter("transferStatus")
fun TextView.setTransferStatus(item: RUpload?) {
    item?.let {
        text = "2 years ago... Maybe the server is unreachable?"
    }
}

@BindingAdapter("updateProgress")
fun ProgressBar.setUpdateProgress(item: RUpload?) {
    item?.let {
        // TODO compute from total size
        progress = it.progress
    }
}
