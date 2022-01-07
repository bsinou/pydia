package org.sinou.android.pydia.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.BrowseActivity
import org.sinou.android.pydia.databinding.ListItemNodeBinding
import org.sinou.android.pydia.room.browse.RTreeNode

class NodeListAdapter(
    private val parentStateID: StateID,
    private val onItemClicked: (stateID: StateID, command: String) -> Unit
) : ListAdapter<RTreeNode, NodeListAdapter.ViewHolder>(TreeNodeDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent).with(parentStateID, onItemClicked)
    }

    class ViewHolder private constructor(val binding: ListItemNodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RTreeNode) {
            binding.node = item
            binding.executePendingBindings()
        }

        fun with(
            parentStateID: StateID,
            onItemClicked: (stateID: StateID, command: String) -> Unit
        ): ViewHolder {

            binding.root.setOnClickListener {
                binding.node?.let {
                    // TODO only navigate with folders.
                    onItemClicked(
                        parentStateID.child(it.name),
                        BrowseActivity.NAVIGATE
                    )
                }
            }
            return this
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemNodeBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class TreeNodeDiffCallback : DiffUtil.ItemCallback<RTreeNode>() {

    override fun areItemsTheSame(oldItem: RTreeNode, newItem: RTreeNode): Boolean {
        return oldItem.encodedState == newItem.encodedState
    }

    override fun areContentsTheSame(oldItem: RTreeNode, newItem: RTreeNode): Boolean {
        // Thanks to Room: RTreeNode is a @Data class and gets equality based on
        // equality of each fields (column) for free.
        return oldItem == newItem
    }
}