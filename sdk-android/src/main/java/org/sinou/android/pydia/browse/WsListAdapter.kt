package org.sinou.android.pydia.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pydio.cells.api.ui.WorkspaceNode
import org.sinou.android.pydia.BrowseActivity
import org.sinou.android.pydia.databinding.ListItemWorkspaceBinding

class WsListAdapter(
    private val onItemClicked: (slug: String, action: String) -> Unit
) : ListAdapter<WorkspaceNode, WsListAdapter.ViewHolder>(WorkspaceNodeDiffCallback()) {

    override fun onBindViewHolder(holder: WsListAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent).with(onItemClicked)
    }

    class ViewHolder private constructor(val binding: ListItemWorkspaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WorkspaceNode) {
            binding.workspace = item
            binding.executePendingBindings()
        }

        fun with(onItemClicked: (slug: String, command: String) -> Unit)
                : ViewHolder {

            binding.root.setOnClickListener {
                binding.workspace?.let {
                    onItemClicked(
                        it.slug,
                        BrowseActivity.actionNavigate
                    )
                }
            }
            return this
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemWorkspaceBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class WorkspaceNodeDiffCallback : DiffUtil.ItemCallback<WorkspaceNode>() {

    override fun areItemsTheSame(oldItem: WorkspaceNode, newItem: WorkspaceNode): Boolean {
        return oldItem.slug == newItem.slug
    }

    override fun areContentsTheSame(oldItem: WorkspaceNode, newItem: WorkspaceNode): Boolean {
        // Thanks to Room: SleepNight is a @Data class and gets equality based on
        // equality of each fields (column) for free.
        return oldItem == newItem
    }
}


