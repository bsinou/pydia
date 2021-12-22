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
import org.sinou.android.pydia.services.AccountService

class AccountListAdapter(private val accountService: AccountService) :
    ListAdapter<RLiveSession, AccountListAdapter.ViewHolder>(SessionDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, accountService)
    }

    // We pass the account service to each view holder via the constructor...
    class ViewHolder(val binding: ListItemAccountBinding, val srv: AccountService) :
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
                Log.i(
                    TAG,
                    "... delete clicked: ${stateID.username}@${stateID.serverUrl} - service: ${srv.toString()}"
                )

                // TODO how can we launch a coroutine from here
//                suspend {
//                    withContext(Dispatchers.IO) {
//                        srv.forgetAccount(item.accountID)
//                    }
//                }
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, srv: AccountService): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAccountBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding, srv)
            }
        }
    }

    class SessionDiffCallback : DiffUtil.ItemCallback<RLiveSession>() {
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
