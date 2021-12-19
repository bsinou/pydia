package org.sinou.android.pydia.account

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.sinou.android.pydia.R
import org.sinou.android.pydia.room.account.Account

class AccountListAdapter : RecyclerView.Adapter<AccountListAdapter.ViewHolder>() {

    private val TAG = "AccountListAdapter"

    var data = listOf<Account>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        // val res = holder.itemView.context.resources
        holder.accountTitle.text = "${item.serverLabel}"
        holder.accountDesc.text = "${item.username}@${item.url}"
        holder.row.setOnClickListener {
            Log.i(TAG, "... item clicked: ${item.username}@${item.url}")
            //val toBrowseIntent = Intent(requireActivity(), AccountActivity::class.java)
//
//            // Launch the account activity with a new intent
//            // TODO not yet plugged
//            // toBrowseIntent.putExtra(AppNames.KEY_DESTINATION, "ServerUrlFragment")
//            startActivity(toBrowseIntent);
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
            .inflate(R.layout.list_item_account, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val row = itemView
        val accountTitle: TextView = itemView.findViewById(R.id.account_title)
        val accountDesc: TextView = itemView.findViewById(R.id.account_description)
        // TODO also retrieve user's avatar for configured account in the current remote
    }

}
