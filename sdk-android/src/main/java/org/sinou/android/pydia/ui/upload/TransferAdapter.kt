package org.sinou.android.pydia.ui.upload

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.databinding.ListItemNodeBinding
import org.sinou.android.pydia.databinding.ListItemTransferBinding
import org.sinou.android.pydia.db.runtime.RUpload

class TransferListAdapter(
    private val onItemClicked: (node: RUpload, command: String) -> Unit
) : ListAdapter<RUpload, TransferListAdapter.ViewHolder>(TransferDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent).with(onItemClicked)
    }

    class ViewHolder private constructor(val binding: ListItemTransferBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RUpload) {
            binding.transfer = item
            binding.executePendingBindings()
        }

        fun with(
            onItemClicked: (node: RUpload, command: String) -> Unit
        ): ViewHolder {

            /*binding.restart.setOnClickListener {
                binding.transfer?.let { onItemClicked(it, AppNames.ACTION_RESTART) }
            }

            binding.listItemMore.setOnClickListener {
                binding.transfer?.let { onItemClicked(it, AppNames.ACTION_CANCEL) }
            }*/

            return this
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemTransferBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class TransferDiffCallback : DiffUtil.ItemCallback<RUpload>() {

    override fun areItemsTheSame(oldItem: RUpload, newItem: RUpload): Boolean {
        return oldItem.encodedState == newItem.encodedState
    }

    override fun areContentsTheSame(oldItem: RUpload, newItem: RUpload): Boolean {
        return oldItem.progress == newItem.progress
                && oldItem.doneTimestamp == newItem.doneTimestamp
                && oldItem.error == newItem.error
    }
}

