package org.sinou.android.pydia.ui.browse

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.databinding.GridItemNodeBinding
import org.sinou.android.pydia.db.nodes.RTreeNode

class NodeGridAdapter(
    private val onItemClicked: (node: RTreeNode, command: String) -> Unit
) : ListAdapter<RTreeNode, NodeGridAdapter.ViewHolder>(TreeNodeDiffCallback()) {

    var tracker: SelectionTracker<String>? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        tracker?.let {
            holder.bind(item, it.isSelected(item.encodedState))
        } ?: run {
            holder.bind(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent).with(onItemClicked)
    }

    fun doGetKey(position: Int): String {
        val item = getItem(position)
        return item.encodedState
    }

    fun doGetPosition(key: String): Int {
        // TODO "brut force" retrieval of an item... Must be enhanced.
        for ((i, node) in currentList.withIndex()) {
            if (node.encodedState == key) {
                return i
            }
        }
        return -1
    }

    class ViewHolder private constructor(val binding: GridItemNodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RTreeNode, activated: Boolean = false) {
            binding.node = item
            binding.nodeCard.isActivated = activated
            binding.nodeDetails.isActivated = activated
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

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
            object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): String = binding.node!!.encodedState
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

class NodeGridItemKeyProvider(private val adapter: NodeGridAdapter) :
    ItemKeyProvider<String>(SCOPE_MAPPED) {

    override fun getKey(position: Int): String {
        return adapter.doGetKey(position)
    }

    override fun getPosition(key: String): Int {
        return adapter.doGetPosition(key)
    }
}

class NodeGridItemDetailsLookup(private val recyclerView: RecyclerView) :

    ItemDetailsLookup<String>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as NodeGridAdapter.ViewHolder)
                .getItemDetails()
        }
        return null
    }
}
