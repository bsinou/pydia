package org.sinou.android.pydia.ui.upload

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.databinding.ListItemNodeBinding
import org.sinou.android.pydia.db.nodes.RTreeNode

class FolderListAdapter(
    private val parentStateID: StateID,
    private val onItemClicked: (stateID: StateID, command: String) -> Unit
) : ListAdapter<RTreeNode, FolderListAdapter.ViewHolder>(PickFolderDiffCallback()) {

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
                    onItemClicked(parentStateID.child(it.name), AppNames.ACTION_OPEN)
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

class PickFolderDiffCallback : DiffUtil.ItemCallback<RTreeNode>() {

    private val tag = "PickFolderDiffCallback"

    override fun areItemsTheSame(oldItem: RTreeNode, newItem: RTreeNode): Boolean {

        val same = oldItem.encodedState == newItem.encodedState
        if (!same) {
            Log.d(tag, "${oldItem.encodedState} != ${newItem.encodedState}")
        }
        return same
    }

    override fun areContentsTheSame(oldItem: RTreeNode, newItem: RTreeNode): Boolean {
        return oldItem.remoteModificationTS == newItem.remoteModificationTS
    }
}