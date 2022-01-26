package org.sinou.android.pydia.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.databinding.ListItemNodeBinding
import org.sinou.android.pydia.db.browse.RTreeNode

class NodeListAdapter(
    private val onItemClicked: (node: RTreeNode, command: String) -> Unit
) : ListAdapter<RTreeNode, NodeListAdapter.ViewHolder>(TreeNodeDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent).with(onItemClicked)
    }

    class ViewHolder private constructor(val binding: ListItemNodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RTreeNode) {
            binding.node = item
            binding.executePendingBindings()
        }

        fun with(
            onItemClicked: (node: RTreeNode, command: String) -> Unit
        ): ViewHolder {

            binding.root.setOnClickListener {
                binding.node?.let { onItemClicked(it, AppNames.ACTION_OPEN) }
            }

            binding.listItemMore.setOnClickListener {
                binding.node?.let { onItemClicked(it, AppNames.ACTION_MORE) }
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
        val same = oldItem.encodedState == newItem.encodedState
        return same
    }

    override fun areContentsTheSame(oldItem: RTreeNode, newItem: RTreeNode): Boolean {
        return areContentsEquals(oldItem, newItem)
    }
}

