package org.sinou.android.pydia.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.databinding.ListItemWorkspaceBinding
import org.sinou.android.pydia.db.accounts.RWorkspace

class WorkspaceListAdapter(
    private val onItemClicked: (slug: String, action: String) -> Unit
) : ListAdapter<RWorkspace, WorkspaceListAdapter.ViewHolder>(WorkspaceDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent).with(onItemClicked)
    }

    class ViewHolder private constructor(val binding: ListItemWorkspaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RWorkspace) {
            binding.workspace = item
            binding.executePendingBindings()
        }

        fun with(onItemClicked: (slug: String, command: String) -> Unit)
                : ViewHolder {

            binding.root.setOnClickListener {
                binding.workspace?.let {
                    onItemClicked(it.slug, AppNames.ACTION_OPEN)
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

class WorkspaceDiffCallback : DiffUtil.ItemCallback<RWorkspace>() {

    override fun areItemsTheSame(oldItem: RWorkspace, newItem: RWorkspace): Boolean {
        return oldItem.slug == newItem.slug
    }

    override fun areContentsTheSame(oldItem: RWorkspace, newItem: RWorkspace): Boolean {
        return oldItem == newItem
    }
}


