package org.sinou.android.pydia.account

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.room.account.RLiveSession
import org.sinou.android.pydia.room.account.RSession

@BindingAdapter("accountImage")
fun ImageView.setAccountImage(item: RSession?) {
    item?.let {

    }
}

@BindingAdapter("account_primary_text")
fun TextView.setAccountPrimaryText(item: RLiveSession?) {
    item?.let{
        var legacy = ""
        if (item.isLegacy) {
            legacy = "(Legacy)"
        }
        text = "${item.serverLabel} ${legacy} "
    }

}

@BindingAdapter("account_secondary_text")
fun TextView.setAccountSecondaryText(item: RLiveSession?) {
    item?.let {
        text = "${item.username}@${item.url} - ${item.authStatus}"
    }
}