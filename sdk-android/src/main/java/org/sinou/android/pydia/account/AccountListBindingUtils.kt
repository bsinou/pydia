package org.sinou.android.pydia.account

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.room.account.RSession

@BindingAdapter("accountImage")
fun ImageView.setAccountImage(item: RSession?) {
    item?.let {

    }
}

@BindingAdapter("account_primary_text")
fun TextView.setAccountPrimaryText(item: RSession?) {
//  FIXME how do you launch a DB request from here.
//    item?.let {
//// FIXME        text = "${item.serverLabel}"
//        val state = StateID.fromId(item.accountID)
//       TextView::class.this.withContext(Dispatchers.IO){
//           val account = CellsApp.instance.accountRepository.accountDB.accountDao().getAccount(state.username, state.serverUrl)
//           text = "a label"
//
//       }
//    }
    text = "a label"
}

@BindingAdapter("account_secondary_text")
fun TextView.setAccountSecondaryText(item: RSession?) {
    item?.let {
        val state = StateID.fromId(item.accountID)
        text = "${state.username}@${state.serverUrl}"
    }
}