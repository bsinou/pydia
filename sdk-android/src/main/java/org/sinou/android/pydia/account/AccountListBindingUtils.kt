package org.sinou.android.pydia.account

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.sinou.android.pydia.room.account.Account

@BindingAdapter("accountImage")
fun ImageView.setAccountImage(item: Account?) {
    item?.let {

    }
}

@BindingAdapter("account_primary_text")
fun TextView.setAccountPrimaryText(item: Account?) {
    item?.let {
        text = "${item.serverLabel}"
        }
}

@BindingAdapter("account_secondary_text")
fun TextView.setAccountSecondaryText(item: Account?) {
    item?.let {
        text = "${item.username}@${item.url}"
    }
}