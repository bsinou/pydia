package org.sinou.android.pydia.upload

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.BrowseActivity
import org.sinou.android.pydia.databinding.ListItemAccountBinding
import org.sinou.android.pydia.room.account.RLiveSession

class SessionListAdapter(
    private val onItemClicked: (stateID: StateID, command: String) -> Unit
) : ListAdapter<RLiveSession, SessionListAdapter.ViewHolder>(SessionsDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent).with(onItemClicked)
    }

    class ViewHolder private constructor(val binding: ListItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RLiveSession) {
            binding.session = item
            binding.executePendingBindings()
        }

        fun with(
            onItemClicked: (stateID: StateID, command: String) -> Unit
        ): ViewHolder {

            binding.root.setOnClickListener {
                binding.session?.let {
                    // TODO only navigate with folders.
                    onItemClicked(
                        StateID.fromId(it.accountID),
                        BrowseActivity.actionNavigate
                    )
                }
            }
            return this
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAccountBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class SessionsDiffCallback : DiffUtil.ItemCallback<RLiveSession>() {

    private val tag = "TreeFolderDiffCallback"

    override fun areItemsTheSame(oldItem: RLiveSession, newItem: RLiveSession): Boolean {
        return oldItem.accountID == newItem.accountID
    }

    override fun areContentsTheSame(oldItem: RLiveSession, newItem: RLiveSession): Boolean {
        var same = oldItem.authStatus == newItem.authStatus
        return same
    }
}
