package org.sinou.android.pydia.account

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.sinou.android.pydia.databinding.ListItemAccountBinding
import org.sinou.android.pydia.room.account.Account

class AccountListAdapter : ListAdapter<Account, AccountListAdapter.ViewHolder>(AccountDiffCallback()) {

    private val TAG = "AccountListAdapter"

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder(val binding: ListItemAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        private val TAG = "ViewHolder<Account>"

        fun bind(item: Account) {

            binding.account = item

            // TODO also retrieve user's avatar for configured account in the current remote
            binding.root.setOnClickListener {
                Log.i(TAG, "... item clicked: ${item.username}@${item.url}")
                //val toBrowseIntent = Intent(requireActivity(), AccountActivity::class.java)
//            // Launch the account activity with a new intent
//            // TODO not yet plugged
//            // toBrowseIntent.putExtra(AppNames.KEY_DESTINATION, "ServerUrlFragment")
//            startActivity(toBrowseIntent);
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAccountBinding.inflate(layoutInflater, parent, false)
//                val view = layoutInflater
//                    .inflate(R.layout.list_item_account, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            // Used when order changes for instance
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            // This relies on Room's auto-generated equality to check
            // if the corresponding view needs to be redrawn.
            // This can be further configured in more complex scenarii
            return oldItem == newItem
        }
    }

}
