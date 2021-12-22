package org.sinou.android.pydia.account

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.BrowseActivity
import org.sinou.android.pydia.databinding.ListItemAccountBinding
import org.sinou.android.pydia.room.account.RLiveSession

class AccountListAdapter(private val onItemClicked: (accountID: String, action: String) -> Unit) :
    ListAdapter<RLiveSession, AccountListAdapter.ViewHolder>(LiveSessionDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, onItemClicked)
    }

    // We pass a click listener to each view holder via the constructor...
    class ViewHolder(
        val binding: ListItemAccountBinding,
        val onItemClicked: (accountID: String, action: String) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val TAG = "ViewHolder<Account>"

        fun bind(item: RLiveSession) {

            binding.session = item

            val stateID = StateID.fromId(item.accountID)
            // TODO also retrieve user's avatar for configured account in the current remote
            binding.root.setOnClickListener {
                Log.i(TAG, "... item clicked: ${stateID.username}@${stateID.serverUrl}")
                val toBrowseIntent = Intent(binding.root.context, BrowseActivity::class.java)
                toBrowseIntent.putExtra(AppNames.EXTRA_STATE, stateID.id)
                startActivity(binding.root.context, toBrowseIntent, null)
            }

            binding.accountDeleteButton.setOnClickListener {
                onItemClicked(item.accountID, "forget")
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(
                parent: ViewGroup,
                onItemClicked: (accountID: String, action: String) -> Unit
            ): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAccountBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding, onItemClicked)
            }
        }
    }

    class LiveSessionDiffCallback : DiffUtil.ItemCallback<RLiveSession>() {
        override fun areItemsTheSame(oldItem: RLiveSession, newItem: RLiveSession): Boolean {
            // Used when order changes for instance
            return oldItem.accountID == newItem.accountID
        }

        override fun areContentsTheSame(oldItem: RLiveSession, newItem: RLiveSession): Boolean {
            // This relies on Room's auto-generated equality to check
            // if the corresponding view needs to be redrawn.
            // This can be further configured in more complex scenarii
            return oldItem == newItem
        }
    }
}
