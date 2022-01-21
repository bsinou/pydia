package org.sinou.android.pydia.ui.browse

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
                binding.node?.let { onItemClicked(it, BrowseFolderFragment.ACTION_OPEN) }
            }

            binding.listItemMore.setOnClickListener {
                binding.node?.let { onItemClicked(it, BrowseFolderFragment.ACTION_MORE) }
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

    private val tag = "TreeNodeDiffCallback"

    override fun areItemsTheSame(oldItem: RTreeNode, newItem: RTreeNode): Boolean {

        val same = oldItem.encodedState == newItem.encodedState
        if (!same) {
            Log.d(tag, "${oldItem.encodedState} != ${newItem.encodedState}")
        }
        return same
    }

    override fun areContentsTheSame(oldItem: RTreeNode, newItem: RTreeNode): Boolean {

        var same = oldItem.remoteModificationTS == newItem.remoteModificationTS

        if (same && newItem.thumbFilename != null) {
            same = newItem.thumbFilename.equals(oldItem.thumbFilename)
        }

        val flagChanged = newItem.isBookmarked == oldItem.isBookmarked
                && newItem.isOfflineRoot == oldItem.isOfflineRoot
                && newItem.isShared == oldItem.isShared


        // With Room: we should get  equality based on equality of each fields (column) for free
        // (RTreeNode is a @Data class). But this doesn't work for now, so we rather only check:
        // remote modif timestamp and thumb filename.

        // More logs to investigate
//        if (!same){
//            Log.d(tag, "Found new content for ${oldItem.encodedState}")
//            Log.d(tag, "Old TS: ${oldItem.remoteModificationTS}, " +
//                    "new TS: ${newItem.remoteModificationTS}")
//            Log.d(tag, "Old thumb: ${oldItem.thumbFilename}, " +
//                    "new thumb: ${newItem.thumbFilename}")
////            Log.d(tag, "old item: \n${Gson().toJson(oldItem)}")
////            Log.d(tag, "new item: \n${Gson().toJson(newItem)}")
//        }
        return same && flagChanged
    }
}