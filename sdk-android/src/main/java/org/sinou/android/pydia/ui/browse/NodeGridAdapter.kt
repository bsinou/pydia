package org.sinou.android.pydia.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.databinding.GridItemNodeBinding
import org.sinou.android.pydia.db.nodes.RTreeNode

class NodeGridAdapter(
    private val onItemClicked: (node: RTreeNode, command: String) -> Unit
) : ListAdapter<RTreeNode, NodeGridAdapter.ViewHolder>(TreeNodeDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent).with(onItemClicked)
    }

    class ViewHolder private constructor(val binding: GridItemNodeBinding) :
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

            binding.gridItemMoreButton.setOnClickListener {
                binding.node?.let { onItemClicked(it, AppNames.ACTION_MORE) }
            }
            return this
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GridItemNodeBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

